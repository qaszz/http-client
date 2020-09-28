package no.kristiania.http;

import java.util.HashMap;
import java.util.Map;

public class QueryString {
    private Map<String, String> parameters = new HashMap<>();
    public QueryString(String queryString){
        for (String parameter : queryString.split("&")) {
            int equalsPos = parameter.indexOf('=');
            String value = parameter.substring(equalsPos+1);
            String parameterName = parameter.substring(0, equalsPos);
            parameters.put(parameterName, value);
        }
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
}
