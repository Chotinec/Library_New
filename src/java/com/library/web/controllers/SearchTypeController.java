package com.library.web.controllers;

import com.library.web.enums.SearchType;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class SearchTypeController {
    
    private Map<String, SearchType> searchList = new HashMap<>();
    
    public SearchTypeController() {
        
        ResourceBundle bundle = ResourceBundle.getBundle("com.library.web.nls.messages", 
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchList.put(bundle.getString("author_name"), SearchType.AUTHOR);
        searchList.put(bundle.getString("book_name"), SearchType.TITLE);
    }

    public Map<String, SearchType> getSearchList() {
        return searchList;
    }  
}
