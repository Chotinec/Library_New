package com.library.web.beans;

import java.io.Serializable;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class LocaleChanger implements Serializable {
    
    private Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    
    public LocaleChanger() {
        
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public void changeLocale(String localeCode) {
        currentLocale = new Locale(localeCode);
    }   
}
