package com.library.web.controllers;

import com.library.web.enums.SearchType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class SearchController implements Serializable{
    
    private SearchType searchType;
    private static Map<String, SearchType> searchlist = new HashMap<String, SearchType>();
    
    public SearchController() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.library.web.nls.messages", 
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchlist.put(bundle.getString("author_name"), searchType.AUTHOR);
        searchlist.put(bundle.getString("book_name"), searchType.TITLE);
    }
    
     public SearchType getSearchType() {
        return searchType;
    }

    public Map<String, SearchType> getSearchlist() {
        return searchlist;
    }
}
