/*
 * 01/12/2005
 *
 * TemplateOptionPanel.java - An option panel to manage RSyntaxTextArea's
 * templates.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.fife.ui.*;
import org.fife.ui.modifiabletable.*;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextarea.RTextArea;


/**
 * An options panel that can be added to an
 * <code>org.fife.ui.OptionsDialog</code> to manage the templates
 * available to all instances of <code>RSyntaxTextArea</code>.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class TemplateOptionPanel extends OptionsDialogPanel {

	private ModifiableTable templateTable;
	private DefaultTableModel tableModel;
	private ResourceBundle msg;


	/**
	 * Constructor.
	 */
	public TemplateOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		msg = ResourceBundle.getBundle(
					"org.fife.ui.rsyntaxtextarea.TemplateOptionPanel");
		setName(msg.getString("Title"));
		Listener listener = new Listener();

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(msg.getString("Templates")));
		add(contentPane);

		tableModel = new DefaultTableModel(new Object[] {
					msg.getString("Template"), msg.getString("Expansion") }, 0);
		templateTable = new ModifiableTable(tableModel);
		JTable table = templateTable.getTable();
		table.setPreferredScrollableViewportSize(new Dimension(300, 300));
		templateTable.addModifiableTableListener(listener);

		contentPane.add(templateTable);
		applyComponentOrientation(orientation);

		// Must create and set row handler later, after this option panel has
		// been added to the parent Options dialog.
		SwingUtilities.invokeLater(() -> {
			RowHandler rowHandler = new TemplateDialog();
			templateTable.setRowHandler(rowHandler);
		});

	}


	@Override
	protected void doApplyImpl(Frame owner) {
		updateCodeTemplateManager();
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return templateTable.getTable();
	}


	/**
	 * Sets the table's contents to be the templates known by the specified
	 * template manager.
	 */
	private void setTemplates(CodeTemplateManager manager) {
		tableModel.setRowCount(0);
		if (manager!=null) {
			CodeTemplate[] templates = manager.getTemplates();
			for (CodeTemplate template : templates) {
				tableModel.addRow(new Object[]{
					template.getID(),
					// Deep copy.
					new TemplateWrapper((CodeTemplate)template.clone()),
				});
			}
		}
	}


	@Override
	protected void setValuesImpl(Frame owner) {
		// Remove all old templates and load the new ones.
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		setTemplates(ctm);
	}


	/**
	 * Updates <code>RSyntaxTextArea</code>'s code template manager
	 * with the information entered here by the user.
	 */
	private void updateCodeTemplateManager() {

		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		if (ctm!=null) {
			int count = tableModel.getRowCount();
			CodeTemplate[] templates = new CodeTemplate[count];
			for (int i=0; i<count; i++) {
				// Make deep copies in case they're clicking "Apply."
				TemplateWrapper w = (TemplateWrapper)tableModel.getValueAt(i, 1);
				templates[i] = (CodeTemplate)w.template.clone();
			}
			ctm.replaceTemplates(templates);
		}

	}


	/**
	 * Listens for events in this option panel.
	 */
	private class Listener implements ModifiableTableListener {

		// A row was added, removed or modified in the template table.
		@Override
		public void modifiableTableChanged(ModifiableTableChangeEvent e) {
			setDirty(true);
		}

	}


	/**
	 * Dialog for modifying a template.
	 */
	class TemplateDialog extends EscapableDialog implements ActionListener,
									DocumentListener, RowHandler {

		private JTextField idField;
		private RTextArea bcTextArea;
		private RTextArea acTextArea;
		private JButton okButton;
		private JButton cancelButton;

		private char[] id;
		private String beforeCaret;
		private String afterCaret;

		TemplateDialog() {

			super(getOptionsDialog());

			ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

			JPanel contentPane = new ResizableFrameContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.setBorder(UIUtil.getEmpty5Border());

			JLabel idLabel = UIUtil.newLabel(msg, "ID");
			idField = new JTextField(20);
			Document doc = idField.getDocument();
			doc.addDocumentListener(this);
			if (doc instanceof AbstractDocument) {
				((AbstractDocument)doc).setDocumentFilter(
					new TemplateNameDocumentFilter());
			}
			idLabel.setLabelFor(idField);
			Box box = createHorizontalBox();
			box.add(idLabel);
			box.add(Box.createHorizontalStrut(5));
			box.add(idField);
			box.add(Box.createHorizontalGlue());
			contentPane.add(box, BorderLayout.NORTH);

			JPanel temp = new JPanel(new GridLayout(2,1, 5,5));
			temp.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
			JLabel label = UIUtil.newLabel(msg, "BeforeCaret");
			bcTextArea = createTextArea();
			label.setLabelFor(bcTextArea);
			RScrollPane sp = new RScrollPane(bcTextArea);
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.NORTH);
			temp2.add(sp);
			temp.add(temp2);
			label = UIUtil.newLabel(msg, "AfterCaret");
			acTextArea = createTextArea();
			label.setLabelFor(acTextArea);
			sp = new RScrollPane(acTextArea);
			temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.NORTH);
			temp2.add(sp);
			temp.add(temp2);
			contentPane.add(temp);

			okButton = UIUtil.newButton(msg, "OK", "OKMnemonic");
			okButton.addActionListener(this);
			okButton.setEnabled(false);
			cancelButton = UIUtil.newButton(msg, "Cancel", "CancelMnemonic");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			contentPane.add(buttons, BorderLayout.SOUTH);

			setContentPane(contentPane);
			setLocationRelativeTo(TemplateOptionPanel.this);
			setModal(true);
			getRootPane().setDefaultButton(okButton);
			applyComponentOrientation(orientation);
			pack();

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.setVisible(false);
			Object source = e.getSource();
			if (source==okButton) {
				id = idField.getText().toCharArray();
				beforeCaret = bcTextArea.getText();
				afterCaret = acTextArea.getText();
			}
			else if (source==cancelButton) {
				id = null;
				beforeCaret = afterCaret = null;
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		private RTextArea createTextArea() {
			// Yuck = RTextArea defaults to black text on white BG always
			JTextArea defaults = new JTextArea();
			RTextArea textArea = new RTextArea(4, 30);
			textArea.setHighlightCurrentLine(false);
			textArea.setBackground(defaults.getBackground());
			textArea.setForeground(defaults.getForeground());
			textArea.setCaretColor(defaults.getCaretColor());
			return textArea;
		}

		@Override
		protected void escapePressed() {
			id = null;
			beforeCaret = afterCaret = null;
			setVisible(false);
		}

		/**
		 * @return The after-caret text entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public String getAfterCaretText() {
			return afterCaret;
		}

		/**
		 * @return The before-caret text entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public String getBeforeCaretText() {
			return beforeCaret;
		}

		/**
		 * @return The id entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public char[] getID() {
			return id;
		}

		@Override
		public Object[] getNewRowInfo(Object[] oldData) {
			if (oldData==null) { // Adding a new row.
				setTitle(msg.getString("AddTemplateTitle"));
				setID(null);
				setBeforeCaretText(null);
				setAfterCaretText(null);
			}
			else { // Modifying a row.
				setTitle(msg.getString("ModifyTemplateTitle"));
				setID((String)oldData[0]);
				TemplateWrapper wrapper = (TemplateWrapper)oldData[1];
				StaticCodeTemplate template = (StaticCodeTemplate)wrapper.template;
				setBeforeCaretText(template.getBeforeCaretText());
				setAfterCaretText(template.getAfterCaretText());
			}
			setVisible(true);
			char[] id = getID();
			Object[] objs = null;
			if (id!=null) {
				String idString = new String(id);
				CodeTemplate template = new StaticCodeTemplate(idString,
						getBeforeCaretText(), getAfterCaretText());
				objs = new Object[] { idString, new TemplateWrapper(template) };
			}
			return objs;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (!okButton.isEnabled())
				okButton.setEnabled(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			int length = idField.getDocument().getLength();
			if (length==0)
				okButton.setEnabled(false);
		}

		public void setAfterCaretText(String text) {
			acTextArea.setText(text);
		}

		public void setBeforeCaretText(String text) {
			bcTextArea.setText(text);
		}

		public void setID(String id) {
			idField.setText(id);
		}

		@Override
		public boolean canModifyRow(int row) {
			return true;
		}

		@Override
		public boolean canRemoveRow(int row) {
			return true;
		}

		/**
		 * Not an override.  Implements <code>RowHandler#updateUI()</code>.
		 */
		@Override
		public void updateUI() {
			SwingUtilities.updateComponentTreeUI(this);
		}

	}


	/**
	 * A document filter that only allows letters, numbers, and
	 * '_' to go through.
	 */
	private static class TemplateNameDocumentFilter extends PickyDocumentFilter{

		@Override
		protected String cleanseImpl(String text) {
			int length = text.length();
			for (int i=0; i<length; i++) {
				if (!RSyntaxUtilities.
					isLetterOrDigit(text.charAt(i)) &&
						text.charAt(i)!='_') {
					text = text.substring(0,i) + text.substring(i+1);
					i--;
					length--;
				}
			}
			return text;
		}

	}


	/**
	 * A wrapper around RSTA's template objects for rendering purposes.
	 */
	private static class TemplateWrapper {

		private CodeTemplate template;

		TemplateWrapper(CodeTemplate template) {
			this.template = template;
		}

		@Override
		public String toString() {
			StaticCodeTemplate template = (StaticCodeTemplate)this.template;
			String temp = template.getBeforeCaretText() +
						template.getAfterCaretText();
			return temp.replaceAll("\\n", "\\\\n").replaceAll("\\t", "\\\\t");
		}

	}


}
