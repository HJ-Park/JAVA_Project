package faceproject;
//This sample uses Apache HttpComponents:

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/
//https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/

import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
//use MS faceAPI, OpenCV
public class FaceDetect {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private JFrame frame;
	private JLabel imageLabel;
	
	private static int frameNum = 0;
	
	
	
	// Replace <Subscription Key> with your valid subscription key.
	private static final String subscriptionKey = "2250cdc2dff84a0da918451b2ddff774";

	// NOTE: You must use the same region in your REST call as you used to
	// obtain your subscription keys. For example, if you obtained your
	// subscription keys from westus, replace "westcentralus" in the URL
	// below with "westus".
	//
	// Free trial subscription keys are generated in the "westus" region. If you
	// use a free trial subscription key, you shouldn't need to change this region.
	private static final String uriBase = "https://koreacentral.api.cognitive.microsoft.com/face/v1.0/detect";

	private static final String imageWithFaces = "{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/c/c3/RH_Louise_Lillian_Gish.jpg\"}";

	//private static final String faceAttributes = "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise";
	private static final String faceAttributes = "gender,smile,glasses,emotion,hair";
	
	public FaceDetect() throws InterruptedException, IOException {
		initGUI();
		videoCapture();
	}
	
	private void initGUI() {
		frame = new JFrame("Camera Input Example");  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.setSize(400,400);  
		imageLabel = new JLabel();
		frame.add(imageLabel);
		frame.setVisible(true);       
	}

	
	public void detect() {
		HttpClient httpclient = new DefaultHttpClient();

		try {
			URIBuilder builder = new URIBuilder(uriBase);

			// Request parameters. All of them are optional.
			builder.setParameter("returnFaceId", "true");
			builder.setParameter("returnFaceLandmarks", "false");
			builder.setParameter("returnFaceAttributes", faceAttributes);

			// Prepare the URI for the REST API call.
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);

			// Request headers.
			//request.setHeader("Content-Type", "application/json");//URL
			request.setHeader("Content-Type", "application/octet-stream");
			request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			// Request body.
			//StringEntity reqEntity = new StringEntity(imageWithFaces);
			File file = new File("img/test.jpg");
			FileEntity reqEntity = new FileEntity(file);//, "ContentType.APPLICATION_OCTET_STREAM"
			request.setEntity(reqEntity);

			// Execute the REST API call and get the response entity.
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				// Format and display the JSON response.
				System.out.println("REST Response:\n");

				String jsonString = EntityUtils.toString(entity).trim();
				if (jsonString.charAt(0) == '[') {
					JSONArray jsonArray = new JSONArray(jsonString);
					System.out.println(jsonArray.toString(2));
				} else if (jsonString.charAt(0) == '{') {
					JSONObject jsonObject = new JSONObject(jsonString);
					System.out.println(jsonObject.toString(2));
				} else {
					System.out.println(jsonString);
				}
			}
		} catch (Exception e) {
			// Display error message.
			System.out.println(e.getMessage());
		}
	}
	
	
	public void videoCapture() throws InterruptedException, IOException {
		ImageProcessor imageProcessor = new ImageProcessor();
		Mat webcamMatImage = new Mat();
 		BufferedImage tempImage;  
		VideoCapture capture = new VideoCapture(0);
		capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,640);
		capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,480);
		//VideoCapture capture = new VideoCapture("videos/vehicle.avi");
		
		if( capture.isOpened()){  
			while (true){  
				capture.read(webcamMatImage);  
								
				if( !webcamMatImage.empty() ){  
					tempImage= imageProcessor.toBufferedImage(webcamMatImage); 
					
					ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
					imageLabel.setIcon(imageIcon);
					frame.pack();  //this will resize the window to fit the image
					Thread.sleep(30);
					
					
					frameNum++;
					
					if(frameNum%200 == 0) {			//200frame per 1 image
						System.out.println(tempImage);
						ImageIO.write(tempImage, "JPG", new File("img/test.jpg"));
						detect();
					}
					
					
				}  
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}
			}  
		}
		else{
			System.out.println("Couldn't open capture.");
		}
	}

}
