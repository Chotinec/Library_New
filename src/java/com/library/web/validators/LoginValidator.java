package com.library.web.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("com.library.web.validators.LoginValidator")
public class LoginValidator implements Validator{

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        
        ResourceBundle bundle = ResourceBundle.getBundle("com.library.web.nls.messages", 
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        
        try {
            String textValue = value.toString();
            
           if (textValue.length() < 5) {
                throw new IllegalArgumentException(bundle.getString("login_length_error"));
           }
           
           if (!Character.isLetter(textValue.charAt(0))) {
               throw new IllegalArgumentException(bundle.getString("first_letter_error"));
           }
           
           if (getTestList().contains(textValue)){
               throw new IllegalArgumentException(bundle.getString("used_name"));
           }
        } catch (IllegalArgumentException e) {
            FacesMessage message = new FacesMessage(e.getMessage());
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            throw new ValidatorException(message);
        }
       
    }
    
     private List<String> getTestList() {
            List<String> list = new ArrayList<>();
            list.add("username");
            list.add("login");
            
            return list;
        }
}
