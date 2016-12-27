
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.ephesoft.dcma.core.hibernate.DynamicHibernateDao;
import com.ephesoft.dcma.core.hibernate.DynamicHibernateDao.ColumnDefinition;
import com.ephesoft.dcma.script.IJDomScript;

/**
 * The <code>ScriptAddNewTable</code> class represents the script execute structure. Writer of scripts plug-in should implement this
 * IScript interface to execute it from the scripting plug-in. Via implementing this interface writer can change its java file at run
 * time. Before the actual call of the java Scripting plug-in will compile the java and run the new class file.
 * 
 * @author Ephesoft
 * @version 1.0
 */
public class ScriptAddNewTable implements IJDomScript {

	private static final String dbUserName = "ephesoft";
	private static final String dbPassword = "ephesoft";
	private static final String dbDriver = "com.mysql.jdbc.Driver";
	private static final String dbConnectionURL = "jdbc:mysql://localhost:3306/ephesoft";
	private static String PAGE = "Page";
	private static String COLUMNS = "Columns";
	private static String DOCUMENT = "Document";
	private static String DOCUMENTS = "Documents";
	private static String TABLE_NAME = "Test Table";
	private static String IDENTIFIER = "Identifier";
	private static String DATATABLES = "DataTables";
	private static String DATATABLE = "DataTable";
	private static String NAME = "Name";
	private static String ROW = "Row";
	private static String HEADER_ROW = "HeaderRow";
	private static String ROWS = "Rows";
	private static String CONFIDENCE = "Confidence";
	private static String FIELD_ORDER_NUMBER = "FieldOrderNumber";
	private static String ROW_COORDINATES = "RowCoordinates";
	private static String X_0 = "x0";
	private static String Y_0 = "y0";
	private static String X_1 = "x1";
	private static String Y_1 = "y1";
	private static String VALUE = "Value";
	private static String VALID = "Valid";
	private static String ALL = "*";
	private static String TABLE = "test_table";
	private String COLUMN = "Column";
	private static String BATCH_LOCAL_PATH = "BatchLocalPath";
	private static String BATCH_INSTANCE_ID = "BatchInstanceIdentifier";
	private static String EXT_BATCH_XML_FILE = "_batch.xml";
	private static String ZIP_FILE_EXT = ".zip";
	private static String IS_RULE_VALID = "isRuleValid";
	private static String TRUE = "true";

	/**
	 * The <code>execute</code> method will execute the script written by the writer at run time with new compilation of java file. It
	 * will execute the java file dynamically after new compilation.
	 * 
	 * @param document {@link Document}
	 */
	@Override
	public Object execute(Document document, String methodName, String documentIdentifier) {
		Exception exception = null;
		try {
			System.out.println("*************  Inside ScriptAddNewTable scripts.");
			System.out.println("*************  Start execution of the ScriptAddNewTable scripts.");

			if (null == document) {
				System.out.println("Input document is null.");
			}
			if (documentIdentifier == null || documentIdentifier.isEmpty()) {
				System.out.println("Document doesnt exists.");
			}
			boolean isWrite = true;
			boolean isDataTablesExist = false;
			Element documents = document.getRootElement().getChild(DOCUMENTS);
			List<?> documentList = documents.getChildren(DOCUMENT);
			Element dataTablesNode = null;
			Element selectedDocumentNode = null;
			List<ColumnDefinition> columnNames = null;
			List<Object[]> data = null;
			try {
				columnNames = getColumnNames();
				data = getTableData();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}

			if (null != documentList) {
				outerloop: for (int index = 0; index < documentList.size(); index++) {
					Element documentNode = (Element) documentList.get(index);
					selectedDocumentNode = documentNode;
					if (null == documentNode) {
						continue;
					}
					List<?> childNodeList = documentNode.getChildren();
					if (null == childNodeList) {
						continue;
					}
					for (int y = 0; y < childNodeList.size(); y++) {

						Element docChildNode = (Element) childNodeList.get(y);
						if (null == docChildNode) {
							continue;
						}
						String nodeName = docChildNode.getName();

						if (null == nodeName) {
							continue;
						}
						if (!nodeName.equals(IDENTIFIER)) {
							continue;
						} else {
							if (!docChildNode.getText().equals(documentIdentifier)) {
								continue outerloop;
							} else {
								break;
							}
						}
					}
					for (int childIndex = 0; childIndex < childNodeList.size(); childIndex++) {
						Element docChildNode = (Element) childNodeList.get(childIndex);
						if (null == docChildNode) {
							continue;
						}
						String nodeName = docChildNode.getName();
						if (null == nodeName) {
							continue;
						}
						if (!nodeName.equals(DATATABLES)) {
							isDataTablesExist = false;
							continue;
						} else {
							isDataTablesExist = true;
							dataTablesNode = docChildNode;
							setTableStructure(document, docChildNode);
							break;
						}
					}
					if (!isDataTablesExist) {
						selectedDocumentNode.addContent(new Element(DATATABLES));
						dataTablesNode = searchNode(selectedDocumentNode, DATATABLES);
						setTableStructure(document, dataTablesNode);
					}
					break;
				}

				populateDataTable(dataTablesNode, columnNames, data);

				// Write the document object to the xml file. Currently following IF block is commented for performance improvement.
				/*if (isWrite) {					
					writeToXML(document);
					System.out.println("*************  Successfully write the xml file for the ScriptAddNewTable scripts.");
				}*/
				// System.out.println("Hello World.");
				System.out.println("*************  End execution of the ScriptAddNewTable scripts.");
			}
		} catch (Exception e) {
			System.out.println("*************  Error occurred in scripts." + e.getMessage());
			e.printStackTrace();
			exception = e;
		}
		return exception;
	}

	private void populateDataTable(Element dataTablesNode, List<ColumnDefinition> columnNames, List<Object[]> data) {
		if (dataTablesNode == null || dataTablesNode.getChildren() == null || dataTablesNode.getChildren().size() == 0) {
			return;
		}
		Element dataTableElement = (Element) dataTablesNode.getChildren().get(0);
		Element headerRowElement = (Element) dataTableElement.getChild(HEADER_ROW);
		Element rowsNode = dataTableElement.getChild(ROWS);
		Element columnsNode = headerRowElement.getChild(COLUMNS);
		for (int index = 0; index < columnNames.size(); index++) {
			populateColumnNames(columnsNode, columnNames.get(index).getColumnName());
		}
		for (int index = 0; index < data.size(); index++) {
			populateTableWithData(data.get(index), rowsNode, headerRowElement);
		}
	}

	private void populateTableWithData(Object[] data, Element rowsNode, Element headerRowElement) {
		rowsNode.addContent(new Element(ROW));
		Element rowNode = (Element) rowsNode.getChildren().get(rowsNode.getChildren().size() - 1);
		rowNode.addContent(new Element(ROW_COORDINATES));
		Element rowCoordinatesNode = (Element) rowNode.getChildren().get(rowNode.getChildren().size() - 1);
		rowCoordinatesNode.addContent(new Element(X_0));
		rowCoordinatesNode.addContent(new Element(Y_0));
		rowCoordinatesNode.addContent(new Element(X_1));
		rowCoordinatesNode.addContent(new Element(Y_1));
		Element ruleElement = new Element(IS_RULE_VALID);
		ruleElement.setText(TRUE);
		rowNode.addContent(ruleElement);
		rowNode.addContent(new Element(COLUMNS));

		Element rowColumnsNode = (Element) rowNode.getChildren().get(rowNode.getChildren().size() - 1);

		for (int index = 0; index < data.length; index++) {
			rowColumnsNode.addContent(new Element(COLUMN));
			Element rowColumnNode = (Element) rowColumnsNode.getChildren().get(rowColumnsNode.getChildren().size() - 1);
			rowColumnNode.addContent(new Element(VALUE));
			rowColumnNode.addContent(new Element(CONFIDENCE));
			Element conf = (Element) rowColumnNode.getChildren().get(rowColumnNode.getChildren().size() - 1);
			conf.setText("100.0");

			rowColumnNode.addContent(new Element(PAGE));
			Element page = (Element) rowColumnNode.getChildren().get(rowColumnNode.getChildren().size() - 1);
			page.setText("100");

			rowColumnNode.addContent(new Element(FIELD_ORDER_NUMBER));
			Element orderNum = (Element) rowColumnNode.getChildren().get(rowColumnNode.getChildren().size() - 1);
			orderNum.setText("0");

			rowColumnNode.addContent(new Element(VALID));
			Element valid = (Element) rowColumnNode.getChildren().get(rowColumnNode.getChildren().size() - 1);
			valid.setText("true");

			populateRowsColumns(rowColumnNode, data[index]);
		}
	}

	private void populateRowsColumns(Element rowColumnNode, Object data) {
		Element valueNode = rowColumnNode.getChild(VALUE);
		if (data != null && !data.toString().isEmpty())
			valueNode.setText(data.toString());
	}

	private void populateColumnNames(Element columnsNode, String columnName) {
		columnsNode.addContent(new Element(COLUMN));
		Element columnNode = (Element) columnsNode.getChildren().get(columnsNode.getChildren().size() - 1);
		columnNode.addContent(new Element(NAME));
		Element name = searchNode(columnNode, NAME);
		name.setText(columnName);

		columnNode.addContent(new Element(CONFIDENCE));
		Element conf = (Element) columnNode.getChildren().get(columnNode.getChildren().size() - 1);
		conf.setText("100.0");

		columnNode.addContent(new Element(FIELD_ORDER_NUMBER));
		Element orderNum = (Element) columnNode.getChildren().get(columnNode.getChildren().size() - 1);
		orderNum.setText("0");

		columnNode.addContent(new Element(VALID));
		Element valid = (Element) columnNode.getChildren().get(columnNode.getChildren().size() - 1);
		valid.setText("true");
	}

	private void setTableStructure(Document document, Element dataTablesNode) {

		dataTablesNode.getChildren().add(0, new Element(DATATABLE));
		Element dataTableNode = (Element) dataTablesNode.getChildren().get(0);
		dataTableNode.addContent(new Element(NAME));
		Element name = (Element) dataTableNode.getChildren().get(dataTableNode.getChildren().size() - 1);
		Date date = new Date();
		name.setText(TABLE_NAME + " " + date.getDate() + " " + date.getMinutes() + " " + date.getSeconds());
		dataTableNode.addContent(new Element(HEADER_ROW));
		Element headerRowNode = searchNode(dataTableNode, HEADER_ROW);
		headerRowNode.addContent(new Element(COLUMNS));
		dataTableNode.addContent(new Element(ROWS));
	}

	/**
	 * The <code>search</code> method will search for a node among child of passed given node.
	 * 
	 * @param parentNode
	 * @param nodeNameToBeSearched
	 */
	private Element searchNode(Element parentNode, String nodeNameToBeSearched) {
		List<?> childNodeList = parentNode.getChildren();
		if (childNodeList != null && childNodeList.size() != 0) {
			for (int index = 0; index < childNodeList.size(); index++) {
				Element childNode = (Element) childNodeList.get(index);
				if (childNode == null) {
					continue;
				}
				if (childNode.getName() == null || childNode.getName().isEmpty()) {
					continue;
				}

				if (childNode.getName().equals(nodeNameToBeSearched)) {
					return childNode;
				}
			}
		}
		return null;
	}

	/**
	 * The <code>writeToXML</code> method will write the state document to the XML file.
	 * 
	 * @param document {@link Document}.
	 */
	private void writeToXML(Document document) {

		String batchLocalPath = null;
		List<?> batchLocalPathList = document.getRootElement().getChildren(BATCH_LOCAL_PATH);
		if (null != batchLocalPathList) {
			batchLocalPath = ((Element) batchLocalPathList.get(0)).getText();
		}

		if (null == batchLocalPath) {
			System.err.println("Unable to find the local folder path in batch xml file.");
			return;
		}

		String batchInstanceID = null;
		List<?> batchInstanceIDList = document.getRootElement().getChildren(BATCH_INSTANCE_ID);
		if (null != batchInstanceIDList) {
			batchInstanceID = ((Element) batchInstanceIDList.get(0)).getText();
		}

		if (null == batchInstanceID) {
			System.err.println("Unable to find the batch instance ID in batch xml file.");
			return;
		}

		String batchXMLPath = batchLocalPath.trim() + File.separator + batchInstanceID + File.separator + batchInstanceID
				+ EXT_BATCH_XML_FILE;
		String batchXMLZipPath = batchXMLPath + ZIP_FILE_EXT;
		System.out.println("batchXMLZipPath************" + batchXMLZipPath);

		OutputStream outputStream = null;
		File zipFile = new File(batchXMLZipPath);

		FileWriter writer = null;
		XMLOutputter out = new com.ephesoft.dcma.batch.encryption.util.BatchInstanceXmlOutputter(batchInstanceID);
		try {
			if (zipFile.exists()) {
				System.out.println("Found the batch xml zip file.");
				outputStream = getOutputStreamFromZip(batchXMLPath, batchInstanceID + EXT_BATCH_XML_FILE);
				out.output(document, outputStream);
			} else {
				writer = new java.io.FileWriter(batchXMLPath);
				out.output(document, writer);
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private List<ColumnDefinition> getColumnNames() throws SQLException {
		List<ColumnDefinition> columnList = null;
		try {
			DynamicHibernateDao dynamicHibernateDao = new DynamicHibernateDao(dbUserName, dbPassword, dbDriver, dbConnectionURL);
			columnList = dynamicHibernateDao.getAllColumnsForTable(TABLE);
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		}
		return columnList;
	}

	private List<Object[]> getTableData() throws SQLException {
		List<Object[]> data = new ArrayList<Object[]>();
		try {
			DynamicHibernateDao dynamicHibernateDao = new DynamicHibernateDao(dbUserName, dbPassword, dbDriver, dbConnectionURL);
			String dbQuery = "Select ";
			dbQuery = dbQuery + ALL + " from " + TABLE;
			SQLQuery query = dynamicHibernateDao.createQuery(dbQuery);
			data = query.list();
		} catch (HibernateException e) {
			System.err.println(e.getMessage());
		}
		return data;
	}

	public static OutputStream getOutputStreamFromZip(final String zipName, final String fileName) throws FileNotFoundException,
			IOException {
		ZipOutputStream stream = null;
		stream = new ZipOutputStream(new FileOutputStream(new File(zipName + ZIP_FILE_EXT)));
		ZipEntry zipEntry = new ZipEntry(fileName);
		stream.putNextEntry(zipEntry);
		return stream;
	}

	// public static void main(String args[]) {
	// System.out.println("entering....");
	// String filePath = "D:\\BI4_batch.xml";
	// try {
	// SAXBuilder sb = new SAXBuilder();
	// Document doc = sb.build(filePath);
	// // new XMLOutputter(Format.getPrettyFormat()).output(doc, System.out);
	// ScriptAddNewTableJDOM functionScript = new ScriptAddNewTableJDOM();
	// functionScript.execute(doc, "", "DOC1");
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

}
