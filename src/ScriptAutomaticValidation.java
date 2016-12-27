import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.ephesoft.dcma.script.IJDomScript;

/**
 * The <code>ScriptAutomaticValidation</code> class represents the script execute structure. Writer of scripts plug-in should implement
 * this IScript interface to execute it from the scripting plug-in. Via implementing this interface writer can change its java file at
 * run time. Before the actual call of the java Scripting plug-in will compile the java and run the new class file.
 * 
 * @author Ephesoft
 * @version 1.0
 */
public class ScriptAutomaticValidation implements IJDomScript {

	private static String DOCUMENT = "Document";
	private static String DOCUMENTS = "Documents";
	private static String DOCUMENT_LEVEL_FIELDS = "DocumentLevelFields";
	private static String TRUE = "true";
	private static String FALSE = "false";
	private static String TYPE = "Type";
	private static String VALUE = "Value";
	private static String BATCH_LOCAL_PATH = "BatchLocalPath";
	private static String BATCH_INSTANCE_ID = "BatchInstanceIdentifier";
	private static String EXT_BATCH_XML_FILE = "_batch.xml";
	private static String VALID = "Valid";
	private static String PATTERN = "dd/MM/yyyy";
	private static String DATE = "DATE";
	private static String LONG = "LONG";
	private static String DOUBLE = "DOUBLE";
	private static String STRING = "STRING";
	private static String ZIP_FILE_EXT = ".zip";

	/**
	 * The <code>execute</code> method will execute the script written by the writer at run time with new compilation of java file. It
	 * will execute the java file dynamically after new compilation.
	 * 
	 * @param document {@link Document}
	 */
	public Object execute(Document document, String methodName, String documentIdentifier) {
		Exception exception = null;
		try {
			System.out.println("*************  Inside ScriptAutomaticValidation scripts.");

			System.out.println("*************  Start execution of ScriptAutomaticValidation scripts.");

			if (null == document) {
				System.out.println("Input document is null.");
			}
			boolean isWrite = true;
			Element validNode = null;
			String valueText = null;
			String typeText = null;
			Element documents = document.getRootElement().getChild(DOCUMENTS);
			List<?> documentList = documents.getChildren(DOCUMENT);
			if (null != documentList) {
				for (int index = 0; index < documentList.size(); index++) {
					Element documentNode = (Element) documentList.get(index);
					if (null == documentNode) {
						continue;
					}
					List<?> childNodeList = documentNode.getChildren();
					if (null == childNodeList) {
						continue;
					}
					validNode = null;
					outerloop: for (int y = 0; y < childNodeList.size(); y++) {
						Element childDoc = (Element) childNodeList.get(y);
						if (null == childDoc) {
							continue;
						}
						String nodeName = childDoc.getName();
						if (null == nodeName) {
							continue;
						}
						if (nodeName.equals(VALID)) {
							validNode = childDoc;
						} else {
							if (nodeName.equals(DOCUMENT_LEVEL_FIELDS)) {
								List<?> dlfNodeList = childDoc.getChildren();
								if (null == dlfNodeList) {
									continue;
								}
								for (int dlf = 0; dlf < dlfNodeList.size(); dlf++) {
									Element dlfDoc = (Element) dlfNodeList.get(dlf);
									if (null == dlfDoc) {
										continue;
									}
									List<?> dlfValueNodeList = dlfDoc.getChildren();
									if (null == dlfValueNodeList) {
										continue;
									}
									valueText = null;
									typeText = null;
									for (int x = 0; x < dlfValueNodeList.size(); x++) {
										Element dlfValueDoc = (Element) dlfValueNodeList.get(x);
										if (null == dlfValueDoc) {
											continue;
										}
										String nName = dlfValueDoc.getName();
										if (nName.equals(VALUE)) {
											valueText = dlfValueDoc.getText();
										} else {
											if (nName.equals(TYPE)) {
												typeText = dlfValueDoc.getText();
											}
										}
										if (null != typeText) {
											boolean isValid = checkValueText(valueText, typeText);
											if (isValid) {
												// validNode.setText(TRUE);
												break;
											} else {
												validNode.setText(FALSE);
												break outerloop;
											}
										}
									}
								}
							}
						}

					}
				}

				// Write the document object to the xml file. Currently following IF block is commented for performance improvement.
				/*if (isWrite) {					
					writeToXML(document);
					System.out.println("*************  Successfully write the xml file for the ScriptAutomaticValidation scripts.");
				}*/
			}
			System.out.println("*************  End execution of the ScriptAutomaticValidation scripts.");
		} catch (Exception e) {
			System.out.println("*************  Error occurred in scripts." + e.getMessage());
			e.printStackTrace();
			exception = e;
		}
		return exception;
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

	/**
	 * The <code>checkValueText</code> method will check the valueText with typeText compatibility.
	 * 
	 * @param valueText {@link String}
	 * @param typeText {@link String}
	 * @return boolean true if pass the test otherwise false.
	 */
	private boolean checkValueText(String valueText, String typeText) {

		boolean isValid = false;
		if (null == valueText || "".equals(valueText)) {
			isValid = false;
		} else {
			if (typeText.equals(DATE)) {
				SimpleDateFormat format = new SimpleDateFormat(PATTERN);
				try {
					format.parse(valueText);
					isValid = true;
				} catch (Exception e) {
					// the value couldn't be parsed by the pattern, return
					// false.
					isValid = false;
				}
			} else {
				if (typeText.equals(LONG)) {
					try {
						Long.parseLong(valueText);
						isValid = true;
					} catch (Exception e) {
						// the value couldn't be parsed by the pattern, return
						// false
						isValid = false;
					}
				} else {
					if (typeText.equals(DOUBLE)) {
						try {
							Float.parseFloat(valueText);
							isValid = true;
						} catch (Exception e) {
							// the value couldn't be parsed by the pattern,
							// return false
							isValid = false;
						}
					} else {
						if (typeText.equals(STRING)) {
							isValid = true;
						} else {
							isValid = false;
						}
					}
				}
			}
		}

		return isValid;
	}

	public static OutputStream getOutputStreamFromZip(final String zipName, final String fileName) throws FileNotFoundException,
			IOException {
		ZipOutputStream stream = null;
		stream = new ZipOutputStream(new FileOutputStream(new File(zipName + ZIP_FILE_EXT)));
		ZipEntry zipEntry = new ZipEntry(fileName);
		stream.putNextEntry(zipEntry);
		return stream;
	}
}
