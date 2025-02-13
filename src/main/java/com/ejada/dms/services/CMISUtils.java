package com.ejada.dms.services;

import com.ejada.commons.errors.exceptions.ErrorCodeException;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.reflections.Reflections.log;

public class CMISUtils {
    @Value("${dms.document.userName}")
    private static String userName;
    @Value("${dms.document.password}")
    private static String password;
    @Value("${dms.document.url}")
    private static String url;
    @Value("${dms.document.repository}")
    private static String repository;
    @Value("${dms.document.bufferLength}")
    private static int bufferLength;
    private static Session session = null;
    public static Session getSession() {
        Map<String, String> parameter = new HashMap<String, String>();

        try{
            System.out.println("**************Enter Session**************");
        if (session == null) {
            SessionFactory factory = SessionFactoryImpl.newInstance();
            // user credentials
            parameter.put(SessionParameter.USER, "SVC_SmartContractSIT");
            parameter.put(SessionParameter.PASSWORD, "2G_Qb@64#oj#qU");
            //connection settings.
            parameter.put(SessionParameter.BROWSER_URL, "http://10.11.34.161:9081/openfncmis/services?wsdl");
            parameter.put(SessionParameter.ATOMPUB_URL, "http://10.11.34.161:9081/openfncmis/atom11");
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.REPOSITORY_ID, "ALRajhiDevOS2");

            System.out.println(parameter);
            session = factory.createSession(parameter);
        }
        return session;
        } catch (CmisBaseException e) {
            System.out.println("**************Error1 Creating session **************");
            log.error("Error creating CMIS session: {}", e.getMessage());
            throw new ErrorCodeException("FAILED_CREATING_SESSION");
        } catch (Exception e) {
            System.out.println("**************Error2 Creating session **************");
            log.error("Unexpected error creating CMIS session 2: {}", e.getMessage());
            throw new ErrorCodeException("FAILED_CREATING_SESSION");
        }
    }

    public static ContentStream getFileContentStream(String path, String fileExtension) throws IOException {
        try (InputStream stream = CMISUtils.class.getClassLoader().getResourceAsStream(path))
        {
            if (stream == null) {
                throw new ErrorCodeException("FILE_NOT_FOUND_IN_CLASSPATH: " + path);
            }
        byte[] content = stream.readAllBytes();
       // ContentStream contentStream
         //   content = Files.readAllBytes(Paths.get(path));
   //         InputStream stream = new ByteArrayInputStream(content);
            // file extension like jpg, tif, doc
            return  new ContentStreamImpl(path,
                    BigInteger.valueOf(content.length), "application/pdf",
                    stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ErrorCodeException("FAILED_READING_FILE_CONTENT");
        }
        //return contentStream;
    }


    public static void writeDocContentToFile(Document doc, String downloadPath) {
        if (doc.getContentStream() != null) {
            InputStream stream = doc.getContentStream().getStream();
            if (stream != null) {
                BufferedOutputStream writer;
                try {
                    writer = new BufferedOutputStream(new FileOutputStream(downloadPath + doc.getContentStreamFileName()));
                    double size = 0.0;
                    int bufferSize;
                    byte[] buffer = new byte[bufferLength];
                    // Loop through the content and write it to the system
                    while ((bufferSize = stream.read(buffer)) != -1) {
                        size += bufferSize;
                        writer.write(buffer, 0, bufferSize);
                    }

                    writer.close();
                    stream.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw new ErrorCodeException("FILE_NOT_FOUND");
                } catch (IOException e) {
                    log.error("Error writing document content to file '{}': {}", downloadPath, e.getMessage());
                    throw new ErrorCodeException("FAILED_WRITING_FILE");
                }
            }
        }
    }

    //get property by name, if it doesn't exist return null
    public static Property getPropertyByName(List<Property<?>> props, String name){
        Iterator iterator = props.iterator();
        while (iterator.hasNext()){
            Property property = (Property)iterator.next();
            if(property.getQueryName().equals(name)){
                return property;
            }
        }
        return null;
    }


    //get string property in XML format
    public static String getStringXML (Property<?> prop)
    {
        String propertyXml = "<" + prop.getQueryName() + ">"
                + prop.getValueAsString() + "</" + prop.getQueryName() + ">";
        return propertyXml;
    }

    //get properties in xml format
    public static String getXMLProperties (List<Property<?>> props, List<String> docProperties)
    {
        String xml = "<?xml version=\"1.0\"encoding=\"UTF-8\"?> <PropertySet>";
        for (String propStr: docProperties) {
            Property property = getPropertyByName(props, propStr);
            xml += getStringXML(property);
        }
        xml += "</PropertySet>";
        return xml;
    }
}

