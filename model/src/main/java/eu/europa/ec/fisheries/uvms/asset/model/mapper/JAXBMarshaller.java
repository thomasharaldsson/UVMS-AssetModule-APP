/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.asset.model.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetException;
import eu.europa.ec.fisheries.uvms.asset.model.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBMarshaller {

    private JAXBMarshaller () {}

    private static Logger log = LoggerFactory.getLogger(JAXBMarshaller.class);

    private static Map<String, JAXBContext> contexts = new HashMap<>();

    /**
     * Marshalls a JAXB Object to a XML String representation
     *
     * @param <T>
     * @param data
     * @return
     * @throws AssetException
     */
    public static <T> String marshallJaxBObjectToString(T data) throws AssetException {
        try {
            JAXBContext jaxbContext = contexts.get(data.getClass().getName());
            if (jaxbContext == null) {
                long before = System.currentTimeMillis();
                jaxbContext = JAXBContext.newInstance(data.getClass());
                contexts.put(data.getClass().getName(), jaxbContext);
                log.debug("Stored contexts: {}", contexts.size());
                log.debug("JAXBContext creation time: {}", (System.currentTimeMillis() - before));
            }
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(data, sw);
            long before = System.currentTimeMillis();
            String marshalled = sw.toString();
            log.debug("StringWriter time: {}", (System.currentTimeMillis() - before));
            return marshalled;
        } catch (JAXBException ex) {
            throw new AssetException(ErrorCode.MARSHALLING_ERROR.getMessage() + data.getClass().getName(), ex, ErrorCode.MARSHALLING_ERROR.getCode());
        }
    }

    /**
     * Unmarshalls A textMessage to the desired Object. The object must be the
     * root object of the unmarshalled message!
     *
     * @param <R>
     * @param textMessage
     * @param clazz pperException
     * @return
     * @throws AssetException
     */
    public static <R> R unmarshallTextMessage(TextMessage textMessage, Class clazz) throws AssetException {
        try {
            JAXBContext jc = contexts.get(clazz.getName());
            if (jc == null) {
                long before = System.currentTimeMillis();
                jc = JAXBContext.newInstance(clazz);
                contexts.put(clazz.getName(), jc);
                log.debug("Stored contexts: {}", contexts.size());
                log.debug("JAXBContext creation time: {}", (System.currentTimeMillis() - before));
            }
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StringReader sr = new StringReader(textMessage.getText());
            StreamSource source = new StreamSource(sr);
            long before = System.currentTimeMillis();
            R object = (R) unmarshaller.unmarshal(source);
            log.debug("Unmarshalling time: {}", (System.currentTimeMillis() - before));
            return object;
        } catch (JMSException | JAXBException ex) {
            throw new AssetException(ErrorCode.UNMARSHALLING_ERROR.getMessage(), ex,  ErrorCode.UNMARSHALLING_ERROR.getCode());
        }
    }
}
