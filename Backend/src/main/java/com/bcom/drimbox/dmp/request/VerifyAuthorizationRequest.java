/*
 *  VerifyAuthorizationRequest.java - DRIMBox
 *  Copyright 2022 b<>com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bcom.drimbox.dmp.request;

import org.w3c.dom.Element;

public class VerifyAuthorizationRequest extends BaseRequest {

	@Override
	protected String actionName() {
		// TODO var-generated method stub
		return "urn:hl7-org:v3:PRPA_IN201307UV02";
	}

	@Override
	protected String serviceName() {
		// TODO var-generated method stub
		return "patients";
	}


	private Element createExtensionElement(String tagName, String rootValue, String extensionValue) {
		Element element = soapRequest.createElement(tagName);
		element.setAttribute("root", rootValue);
		element.setAttribute("extension", extensionValue);
		return element;
	}

	private Element createCodeElement(String tagName, String codeValue) {
		Element element = soapRequest.createElement(tagName);
		element.setAttribute("code", codeValue);
		return element;
	}


	public VerifyAuthorizationRequest(String patientINS) {
		super();


		Element pPRPA_IN201307UV02 = soapRequest.createElement("urn:PRPA_IN201307UV02");
		pPRPA_IN201307UV02.setAttribute("ITSVersion", "XML_1.0");
		pPRPA_IN201307UV02.setAttribute("xmlns", "urn:hl7-org:v3");
		body.appendChild(pPRPA_IN201307UV02);

		Element pId = createExtensionElement("id", "1.3.6.1.4.1.48364.1.1", "1633a409-3d61-4869-aed8-74d92ae20272");
		pPRPA_IN201307UV02.appendChild(pId);

		Element pCreationTime = soapRequest.createElement("creationTime");
		pCreationTime.setAttribute("value", "20210519174507");
		pPRPA_IN201307UV02.appendChild(pCreationTime);

		Element pInterationId = createExtensionElement("interactionId", "2.16.840.1.113883.1.6", "PRPA_IN201307UV02");
		pPRPA_IN201307UV02.appendChild(pInterationId);

		Element pProcessingCode = createCodeElement("processingCode", "D");
		pPRPA_IN201307UV02.appendChild(pProcessingCode);

		Element pProcessingModeCode = createCodeElement("processingModeCode", "T");
		pPRPA_IN201307UV02.appendChild(pProcessingModeCode);

		Element pacceptAckCode = createCodeElement("acceptAckCode", "AL");
		pPRPA_IN201307UV02.appendChild(pacceptAckCode);

		Element preceiver = soapRequest.createElement("receiver");
		preceiver.setAttribute("typeCode", "RCV");
		pPRPA_IN201307UV02.appendChild(preceiver);

		Element pdevice = soapRequest.createElement("device");
		pdevice.setAttribute("classCode", "DEV");
		pdevice.setAttribute("determinerCode", "INSTANCE");
		preceiver.appendChild(pdevice);

		Element pIdDevice = soapRequest.createElement("id");
		pIdDevice.setAttribute("root", "1.2.250.1.213.4.1.1.1");
		pdevice.appendChild(pIdDevice);

		Element pSoftwareName = soapRequest.createElement("softwareName");
		pSoftwareName.appendChild(soapRequest.createTextNode("DMP"));
		pdevice.appendChild(pSoftwareName);

		Element pSender = soapRequest.createElement("sender");
		pSender.setAttribute("typeCode", "SND");
		pPRPA_IN201307UV02.appendChild(pSender);

		Element psdevice = soapRequest.createElement("device");
		psdevice.setAttribute("classCode", "DEV");
		psdevice.setAttribute("determinerCode", "INSTANCE");
		pSender.appendChild(psdevice);

		Element psIdDevice = soapRequest.createElement("id");
		psIdDevice.setAttribute("root", "1.3.6.1.4.1.48364");
		psdevice.appendChild(psIdDevice);

		Element psSoftwareName = soapRequest.createElement("softwareName");
		psSoftwareName.appendChild(soapRequest.createTextNode("DRIMbox"));
		psdevice.appendChild(psSoftwareName);

		Element pAttentionLine = soapRequest.createElement("attentionLine");
		pAttentionLine.setAttribute("xsi:nil", "true");
		pAttentionLine.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		pPRPA_IN201307UV02.appendChild(pAttentionLine);

		Element pcontrolActProcess = soapRequest.createElement("urn:controlActProcess");
		pcontrolActProcess.setAttribute("classCode", "CACT");
		pcontrolActProcess.setAttribute("moodCode", "EVN");
		pPRPA_IN201307UV02.appendChild(pcontrolActProcess);

		Element pReasonCode = soapRequest.createElement("reasonCode");
		pReasonCode.setAttribute("code", "TEST_EXST");
		pReasonCode.setAttribute("codeSystem", "1.2.250.1.213.1.1.4.11");
		pReasonCode.setAttribute("displayName", "Test d'existence de dossier");
		pcontrolActProcess.appendChild(pReasonCode);

		Element pqueryByParameter = soapRequest.createElement("queryByParameter");
		pcontrolActProcess.appendChild(pqueryByParameter);

		Element pqueryId = soapRequest.createElement("queryId");
		pqueryId.setAttribute("root", "1.3.6.1.4.1.48364.2");
		pqueryId.setAttribute("extension", "87d5fd8f-72b2-43bf-938c-0402bf850d25");
		pqueryByParameter.appendChild(pqueryId);

		Element pstatusCode = soapRequest.createElement("statusCode");
		pstatusCode.setAttribute("code", "new");
		pqueryByParameter.appendChild(pstatusCode);

		Element pparameterList = soapRequest.createElement("parameterList");
		pqueryByParameter.appendChild(pparameterList);

		Element pDataSource = soapRequest.createElement("dataSource");
		pDataSource.setAttribute("xsi:nil", "true");
		pDataSource.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		pparameterList.appendChild(pDataSource);

		Element pPatientIdentifier = soapRequest.createElement("patientIdentifier");
		pparameterList.appendChild(pPatientIdentifier);

		Element pValue = soapRequest.createElement("value");
		pValue.setAttribute("root", "1.2.250.1.213.1.4.10");
		pValue.setAttribute("extension", patientINS);
		pPatientIdentifier.appendChild(pValue);

		Element psemanticsText = soapRequest.createElement("semanticsText");
		psemanticsText.appendChild(soapRequest.createTextNode("Patient.id"));
		pPatientIdentifier.appendChild(psemanticsText);
	}


}