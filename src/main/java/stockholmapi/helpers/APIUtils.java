package stockholmapi.helpers;

import spark.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class APIUtils {

    public static final String API_DESCRIPTION = "Description";
    public static final String API_HUVUDBILD = "Image";
    public static final String API_POST_ADDRESS = "PostalAddress";
    public static final String API_ZIP = "PostalCode";
    public static final String API_SHORT_DESC = "ShortDescription";
    public static final String API_STREET_ADDRESS = "StreetAddress";

    public static class URLS {

        private static final String ID_PLACEHOLDER = "{#type_id#}";

        private static final String BASE_URL = "http://api.stockholm.se";
        public static final String BASIC_INFO_PLACEHOLDER = "/ServiceGuideService/ServiceUnitTypes/" + ID_PLACEHOLDER + "/ServiceUnits/json?apikey=";
        public static final String DETAILED_INFO_PLACEHOLDER = "/ServiceGuideService/DetailedServiceUnits/" + ID_PLACEHOLDER + "/json?apikey=";

        public static final String LEKPLATSER = "9da341e4-bdc6-4b51-9563-e65ddc2f7434";
        public static final String BADPLATSER = "c1aca600-af0c-43f9-bf6c-cd7b4ec4b2d1";
        public static final String UTOMHUSBASSÄNGER = "c2b4f305-7304-4eb2-88b0-d67c95f01d7a";
        public static final String MOTIONSSPÅR = "a4116a6a-af53-4672-b492-01d7adeae987";
        public static final String MUSEUM = "ad53d167-dba4-4000-b9b0-89380b89e831";

        public static final String IMAGE_PLACEHOLDER = "/ServiceGuideService/ImageFiles/" + ID_PLACEHOLDER + "/Data?apikey=";

        public static final String GEOGRAPHICAL_AREAS = "/ServiceGuideService/GeographicalAreas/json?apikey=";
        public static final String MULTI_SERVICE_GUIDE_SERVICE_DETAILED_WITH_ID_PLACEHOLDER = "/ServiceGuideService/DetailedServiceUnits/json?ids={#ids#}&serviceunittypeid=" +
                ID_PLACEHOLDER + "&apikey=";

        public static URL urlHelper(String whatInfo, String id, String apiKey) throws Exception{
            return new URL((BASE_URL + whatInfo + apiKey).replace(ID_PLACEHOLDER, id));
        }

        public static URL urlHelper(String whatInfo, String apiKey) throws Exception {
            return new URL(BASE_URL + whatInfo + apiKey);
        }
    }

    public static String stupidStockholmAPIJSONToNotStupidJSON(String stupidJSON) {
        return stupidJSON.replace("Value\":{", "Value2\":{").replace("Values\":{","Values2\":{");
    }

    public static String getUrl(URL url) throws Exception {
        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static String getUrl(String urlStr) throws Exception {
        return getUrl(new URL(urlStr));
    }

    public static byte[] imageUrlToByteArray(URL url) throws Exception {
        InputStream is = null;
        try {
            is = url.openStream ();
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace ();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        throw new Exception();
    }
}
