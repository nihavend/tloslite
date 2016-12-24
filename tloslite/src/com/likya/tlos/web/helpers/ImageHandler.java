/*******************************************************************************
 * Copyright 2014 Likya Teknoloji
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.likya.tlos.web.helpers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.sun.net.httpserver.HttpExchange;

public class ImageHandler extends TlosHttpHandler {

	private String contentPath = "/webroot/"; //$NON-NLS-1$
	
	public ImageHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ImageHandler(String contentPath) {
		super();
		this.contentPath = contentPath;
	}
	
	public void handle(HttpExchange t) throws IOException {
//		InputStream is;
		OutputStream os;

//		is = t.getRequestBody();

		// URI uri = t.getRequestURI();

		// String path = uri.getPath();
		
		// System.out.println(path);
		// String bufferString = null;
		// int i;
		// while ((i = is.read()) != -1) {
		// bufferString = bufferString + (char)i;
		// }
		
		URI uri = t.getRequestURI();
		StringTokenizer stringTokenizer = new StringTokenizer(uri.toString(), "/"); //$NON-NLS-1$

		String contextStr = stringTokenizer.nextToken();
		String imageName = stringTokenizer.nextToken();
		
		os = t.getResponseBody();

		BufferedImage image = readImageFromLocal(contextStr, imageName);
		
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		
		final int JPG = 0;
		final int GIF = 1;
		final int PNG = 2;
		
		int imageType = JPG;
		
		if(imageName.substring(imageName.indexOf(".") + 1).toLowerCase().equals("gif")) { //$NON-NLS-1$ //$NON-NLS-2$
			imageType = GIF;
		} else if(imageName.substring(imageName.indexOf(".") + 1).toLowerCase().equals("png")) { //$NON-NLS-1$ //$NON-NLS-2$
			imageType = PNG;
		}
		
		switch (imageType) {
		case JPG:
			ImageIO.write(image, "pnm", bas); //$NON-NLS-1$

			byte [] data = bas.toByteArray();
			t.sendResponseHeaders(200, data.length);

			writeJpgToRemote(os, image, "jpg"); //$NON-NLS-1$
			
			break;

		case GIF:
			ImageIO.write(image, "GIF", bas); //$NON-NLS-1$

			byte [] data1 = bas.toByteArray();
			t.sendResponseHeaders(200, data1.length);

			writeGifToRemote(os, image, "gif"); //$NON-NLS-1$
			
			break;

		case PNG:
			ImageIO.write(image, "PNG", bas); //$NON-NLS-1$

			byte [] data2 = bas.toByteArray();
			t.sendResponseHeaders(200, data2.length);

			writePngToRemote(os, image, "png"); //$NON-NLS-1$
			
			break;

		} 
		// String response;
		// os.write(response.getBytes());
		os.close();
	}

	private BufferedImage readImageFromLocal(String contextName, String imageName) {
		BufferedImage image = null;
		try {
			
			/**
			 * Serkan Taş 09.06.2008 1.43
			 * Eğer diksten ve src dizinin de bir üst seviyesinden okuma yapılacak ise :
			 * Read from a file
			 * 	File sourceimage = new File("webroot\\img\\likya.jpg");
			 * 	image = ImageIO.read(sourceimage);
			 * Eğer jar içinden yada src dizini içinden okuma yapılacak ise;
			 * Aşağıdaki gibi kodlama yapılmalı.
			 */
			InputStream inputStream = this.getClass().getResourceAsStream(contentPath + contextName + "/" + imageName); //$NON-NLS-1$
			// System.out.println(inputStream == null ? "Bad" : "Good");
			image = ImageIO.read(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
	
	private void writeJpgToRemote(OutputStream os, BufferedImage bufferedImage, String imagePrefix) {


		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(imagePrefix);
	    ImageWriter writer = (ImageWriter)writers.next();

	    ImageOutputStream ios;
		try {
			ios = ImageIO.createImageOutputStream(os);
		    writer.setOutput(ios);
		    writer.write(bufferedImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeGifToRemote(OutputStream os, BufferedImage bufferedImage, String imagePrefix) {



	    ImageOutputStream ios;
		try {
			ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(bufferedImage, "GIF", ios); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	private void writePngToRemote(OutputStream os, BufferedImage bufferedImage, String imagePrefix) {



	    ImageOutputStream ios;
		try {
			ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(bufferedImage, "PNG", ios); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
