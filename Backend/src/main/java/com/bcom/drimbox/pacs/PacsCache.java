/*
 *  PacsCache.java - DRIMBox
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

package com.bcom.drimbox.pacs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.bcom.drimbox.api.DRIMboxConsoAPI;
import com.bcom.drimbox.utils.PrefixConstants;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import io.quarkus.logging.Log;
import io.vertx.core.Vertx;

@Singleton
public class PacsCache {
	static class DicomCacheInstance {
		// Instance UID => file
		Map<String, byte[]> dicomFiles = new HashMap<>();
		Boolean complete = false;
	}

	// StudyUID =>
	//  - SeriesUID
	//      - InstanceUID
	//      - InstanceUID
	//  - SeriesUID
	//      - InstanceUID
	// StudyUID =>
	//  - SeriesUID
	//  - SeriesUID
	Map<String, Map<String, DicomCacheInstance>> dicomCache = new HashMap<>();

	private final Vertx vertx;

	@Inject
	public PacsCache(Vertx vertx) {
		this.vertx = vertx;
	}


	/**
	 * Add new entry to the cache. It will fetch all instances of given study and
	 * series UID to store it in the cache.
	 *
	 * This function is non-blocking and will be executed in another thread
	 *
	 * @param drimboxSourceURL Source drimbox URL
	 * @param studyUID Study UID
	 * @param seriesUID Series UID
	 */
	public void addNewEntry(String drimboxSourceURL, String studyUID, String seriesUID) {
		// Do not rebuild if already here
		if (dicomCache.containsKey(studyUID) && dicomCache.get(studyUID).containsKey(seriesUID))
			return;

		vertx.executeBlocking(promise -> {
			Log.info("Starting cache build...");
			Log.info("Starting WADO (series) request : " + seriesUID);

			Map<String, DicomCacheInstance> map = new HashMap<>();
			map.put(seriesUID, new DicomCacheInstance());
			
			if(dicomCache.containsKey(studyUID)) {
				dicomCache.get(studyUID).put(seriesUID, new DicomCacheInstance());
			}
			else {
			dicomCache.put(studyUID, map);
			}
			buildEntry(drimboxSourceURL, studyUID, seriesUID);

			promise.complete();
		}, res -> 
		Log.info("Cache built")
				);
	}

	// TODO : handle study/series not present

	private DicomCacheInstance getCacheInstance(String studyUID, String seriesUID) {
		return dicomCache.get(studyUID).get(seriesUID);
	}


	/**
	 * Get dicom file in cache.
	 *
	 * If it is not available it will wait for the buildCache to emit the requested
	 * file.
	 *
	 * @param studyUID Study UID
	 * @param seriesUID Series UID
	 * @param instanceUID Instance UID
	 *
	 * @return Dicom file corresponding to the UIDs.
	 * It may not be available right away as the cache can take some time to be built.
	 */
	Map<String, CompletableFuture<byte[]>> waitingFutures = new HashMap<>();
	public Future<byte[]> getDicomFile(String studyUID, String seriesUID, String instanceUID) {
		CompletableFuture<byte[]> completableFuture = new CompletableFuture<>();

		DicomCacheInstance instance = getCacheInstance(studyUID, seriesUID);

		if (instance.dicomFiles.containsKey(instanceUID)) {
			//Log.info("[CACHE] Available " + instanceUID);
			completableFuture.complete(instance.dicomFiles.get(instanceUID));
		} else {
			Log.info("[CACHE] Waiting for : " + instanceUID);
			waitingFutures.put(instanceUID, completableFuture);
//			vertx.eventBus().consumer(instanceUID).handler( m-> {
//				Log.info("[CACHE] Sending image : " + instanceUID);
//				completableFuture.complete((byte[]) m.body());
//			}
//					);
//
		}

		return completableFuture;
	}

	private interface BoundaryFunc { String getBoundary(String contentType); }
	private void buildEntry(String drimboxSourceURL, String studyUID, String seriesUID) {
		String serviceURL = DRIMboxConsoAPI.HTTP_PROTOCOL + drimboxSourceURL + "/" + PrefixConstants.DRIMBOX_PREFIX + "/" + PrefixConstants.STUDIES_PREFIX + "/" + studyUID + "/series/" + seriesUID;
		//String serviceURL = "http://localhost:8081/dcm4chee-arc/aets/AS_RECEIVED/rs"  + "/" + STUDIES_PREFIX + "/" + studyUID + "/series/" + seriesUID;
		try {
			final URL url = new URL(serviceURL);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			BoundaryFunc boundaryManager = (String contentType) -> {
				String[] respContentTypeParams = contentType.split(";");
				for (String respContentTypeParam : respContentTypeParams)
					if (respContentTypeParam.replace(" ", "").startsWith("boundary="))
						return respContentTypeParam
								.substring(respContentTypeParam.indexOf('=') + 1)
								.replaceAll("\"", "");

				return null;
			};

			String boundary = boundaryManager.getBoundary(connection.getContentType());
			if (boundary == null) {
				Log.fatal("Invalid response. Unpacking of parts not possible.");
				throw new RuntimeException();
			}



			DicomCacheInstance dc = getCacheInstance(studyUID, seriesUID);

			new MultipartParser(boundary).parse(new BufferedInputStream(connection.getInputStream()), new MultipartParser.Handler() {
				@Override
				public void bodyPart(int partNumber, MultipartInputStream multipartInputStream) throws IOException {
					Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
					try {
						byte[] rawDicomFile = multipartInputStream.readAllBytes();
						DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(rawDicomFile));
						Attributes dataSet = dis.readDataset();
						String instanceUID = dataSet.getString(Tag.SOPInstanceUID);

						//Log.info("Received file " + instanceUID);
						dc.dicomFiles.put(instanceUID, rawDicomFile);

						if (waitingFutures.containsKey(instanceUID)) {
							Log.info("[CACHE] Publish file " + instanceUID);
							waitingFutures.get(instanceUID).complete(rawDicomFile);
							waitingFutures.remove(instanceUID);
						}

					} catch (Exception e) {
						Log.fatal("Failed to process Part #" + partNumber + headerParams, e);
					}
				}
			});

			dc.complete = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}



	}

}