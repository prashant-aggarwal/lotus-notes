package com.lotus.notes.lotusnotes;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lotus.domino.AgentBase;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.RichTextItem;
import lotus.domino.Session;

@RestController
@RequestMapping("/notes")
public class LotusNotesFileUploadController extends AgentBase {

	@Autowired
	Environment mEnvironment;

	Database mNotesServerDatabase = null;
	Session mNotesSession = null;

	@GetMapping(value = "/upload")
	public String uploadFileToLotusNotes() {
		NotesMain();
		
		String filePath = mEnvironment.getProperty("filePath");
		uploadFiles(filePath);
		
		return "Uploaded Successfully";
	}

	@GetMapping(value = "/download")
	public String downloadFileToLotusNotes() {
		NotesMain();
		
		String documentUnivId = mEnvironment.getProperty("notesDocumentUNID");
		downloadFiles(documentUnivId);

		return "Downloaded Successfully";
	}

	@Override
	public void NotesMain() {

		try {
			String password = mEnvironment.getProperty("notesPassword");
			String noteServer = mEnvironment.getProperty("notesServer");
			String noteDB = mEnvironment.getProperty("notesDatabase");	

			NotesThread.sinitThread();
			System.out.println(".................... Notes Thread Started ................");

			try {
				if (mNotesSession == null) {
					// Lotus Notes Session Creation
					mNotesSession = NotesFactory.createSessionWithFullAccess(password);
					System.out.println(".................... Started Notes Session ................");

					// Getting Lotus Notes DataBase Object
					mNotesServerDatabase = mNotesSession.getDatabase(noteServer, noteDB, false);
					System.out.println(".................... Retrieved Notes Database Object ................");

					String dbID = mNotesServerDatabase.getURL();
					System.out.println(
							".................... Notes database :" + dbID + " Object Fetched ................");
					System.out.println(".................... Notes database Size :" + mNotesServerDatabase.getSize()
							+ " Bytes ................");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception in inner catch.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in outer catch.");
		}
	}

	private void downloadFiles(String documentUnivId) {
		try {
			Document document = mNotesServerDatabase.getDocumentByUNID(documentUnivId);
			if (document != null) {
				for (Object att : mNotesSession.evaluate("@AttachmentNames", document)) {
					if (att == null || att.toString().isEmpty()) {
						continue;
					}

					EmbeddedObject eb = document.getAttachment(att.toString());
					System.err.println("eb.getName() : " + eb.getName());
					System.err.println("eb.getFileSize(): " + eb.getFileSize());
					eb.extractFile(eb.getName());
				}

				System.err.println("doc.getUniversalID(): " + document.getUniversalID());
				System.err.println("doc.getSize(): " + document.getSize());
				System.err.println("doc.getURL(): " + document.getURL());
			}

			/*
			 * DocumentCollection documentCollection = _serverDatabase.getAllDocuments();
			 * Document doc = documentCollection.getFirstDocument(); while (doc != null) {
			 * for (Object att : _lotesNotesSession.evaluate("@AttachmentNames", doc)) { if
			 * (att == null || att.toString().isEmpty()) { continue; }
			 * 
			 * EmbeddedObject eb = doc.getAttachment(att.toString());
			 * System.err.println("eb.getName() : " + eb.getName());
			 * System.err.println("eb.getFileSize(): " + eb.getFileSize()); }
			 * 
			 * System.err.println("doc.getUniversalID(): " + doc.getUniversalID());
			 * System.err.println("doc.getSize(): " + doc.getSize());
			 * System.err.println("doc.getURL(): " + doc.getURL());
			 * 
			 * doc = documentCollection.getNextDocument(); }
			 */
			System.out.println(".................... File downloaded successfully ....................");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadFiles(String filePath) {
		try {
			File dir = new File(filePath);
			System.out.println(".................... File Path ...................." + dir);

			System.out.println(".................... Creating Notes Doument ....................");
			Document doc = mNotesServerDatabase.createDocument();
			doc.replaceItemValue("docname", "Prashant Test Document");
			System.out.println(".................... Created Notes Doument ...................." + doc.getUniversalID());

			RichTextItem body = doc.createRichTextItem("body");

			String[] fileList = dir.list();
			if (fileList != null) {
				for (int i = 0; i < fileList.length; ++i) {
					System.out.println(".................... Upload file is ................" + filePath + "\\" + fileList[i]);

					body.embedObject(EmbeddedObject.EMBED_ATTACHMENT, null, filePath + "\\" + fileList[i], null);
					doc.computeWithForm(true, false);
					doc.save(true, true);
				}
			} else {
				System.out.println(".................... Upload file is ................" + filePath);
				
				body.embedObject(EmbeddedObject.EMBED_ATTACHMENT, null, filePath, null);
				doc.computeWithForm(true, false);
				doc.save(true, true);
			}
			
			System.out.println(".................... File(s) uploaded successfully ....................");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
