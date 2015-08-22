/**
 * HpcFileSetCodec.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao.mongo.codec;

import static gov.nih.nci.hpc.dao.mongo.codec.HpcDecoder.decodeFile;
import gov.nih.nci.hpc.domain.dataset.HpcFile;
import gov.nih.nci.hpc.domain.dataset.HpcFileSet;

import java.util.List;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * <p>
 * HPC File Set Codec. 
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcFileSetCodec extends HpcCodec<HpcFileSet>
{ 
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Default Constructor.
     * 
     */
    public HpcFileSetCodec()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // Codec<HpcFileSet> Interface Implementation
    //---------------------------------------------------------------------//  
    
	@Override
	public void encode(BsonWriter writer, HpcFileSet fileSet,
					   EncoderContext encoderContext) 
	{
		Document document = new Document();
 
		// Extract the data from the domain object.
		String name = fileSet.getName();
		String description = fileSet.getDescription();
		String comments = fileSet.getComments();
		List<HpcFile> files = fileSet.getFiles();
 
		// Set the data on the BSON document.
		if(name != null) {
		   document.put(FILE_SET_NAME_KEY, name);
		}
		if(description != null) {
		   document.put(FILE_SET_DESCRIPTION_KEY, description);
		}
		if(comments != null) {
		   document.put(FILE_SET_COMMENTS_KEY, comments);
		}
		if(files != null && files.size() > 0) {
		   document.put(FILE_SET_FILES_KEY, files);
		}

		getRegistry().get(Document.class).encode(writer, document, 
				                                 encoderContext);
	}
 
	@Override
	@SuppressWarnings("unchecked")
	public HpcFileSet decode(BsonReader reader, 
			                 DecoderContext decoderContext) 
	{
		// Get the BSON Document.
		Document document = 
				 getRegistry().get(Document.class).decode(reader, 
						                                  decoderContext);
		
		// Map the BSON Document to a domain object.
		HpcFileSet fileSet = new HpcFileSet();
		fileSet.setName(document.getString(FILE_SET_NAME_KEY));
		fileSet.setDescription(document.getString(FILE_SET_DESCRIPTION_KEY));
		fileSet.setComments(document.getString(FILE_SET_COMMENTS_KEY));
		
		List<Document> fileDocuments = 
				       (List<Document>) document.get(FILE_SET_FILES_KEY);
		if(fileDocuments != null) {
		   for(Document fileDocument : fileDocuments) {
			   fileSet.getFiles().add(decodeFile(fileDocument, decoderContext, 
					                             getRegistry()));
		   }
		}
		
		return fileSet;
	}
	
	@Override
	public Class<HpcFileSet> getEncoderClass() 
	{
		return HpcFileSet.class;
	}
}

 