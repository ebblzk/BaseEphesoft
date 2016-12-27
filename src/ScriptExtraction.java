import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.ephesoft.dcma.script.IJDomScript;

/**
 * The <code>ScriptExtraction</code> class represents the ScriptExtraction structure. Writer of scripts plug-in should implement this
 * IScript interface to execute it from the scripting plug-in. Via implementing this interface writer can change its java file at run
 * time. Before the actual call of the java Scripting plug-in will compile the java and run the new class file.
 * 
 * @author Ephesoft
 * @version 1.0
 */
public class ScriptExtraction implements IJDomScript {

	private static final String BATCH_LOCAL_PATH = "BatchLocalPath";
	private static final String BATCH_INSTANCE_ID = "BatchInstanceIdentifier";
	private static final String EXT_BATCH_XML_FILE = "_batch.xml";
	private static String ZIP_FILE_EXT = ".zip";

	public Object execute(Document documentFile, String methodName, String docIdentifier) {
		Exception exception = null;
		try {
			System.out.println("*************  Inside ScriptExtraction scripts.");
			System.out.println("*************  Start execution of the ScriptExtraction scripts.");

			if (null == documentFile) {
				System.out.println("Input document is null.");
			}
			boolean isWrite = false;

		// Write the document object to the xml file. Currently following IF block is commented for performance improvement.
		/*if (isWrite) {					
			writeToXML(documentFile);
			System.out.println("*************  Successfully write the xml file for the ScriptExtraction scripts.");
			System.out.println("*************  End execution of the ScriptExtraction scripts.");
		}*/
		} catch (Exception e) {
			System.out.println("*************  Error occurred in scripts." + e.getMessage());
			exception = e;
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

	public static OutputStream getOutputStreamFromZip(final String zipName, final String fileName) throws FileNotFoundException,
			IOException {
		ZipOutputStream stream = null;
		stream = new ZipOutputStream(new FileOutputStream(new File(zipName + ZIP_FILE_EXT)));
		ZipEntry zipEntry = new ZipEntry(fileName);
		stream.putNextEntry(zipEntry);
		return stream;
	}
}
