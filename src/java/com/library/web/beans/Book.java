package com.library.web.beans;

import com.library.web.db.Database;
import java.awt.Image;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Book implements Serializable {
    
    private long id;
    private String name;
    private byte[] content;
    private int pageCount;
    private String isbn;
    private String genre;
    private String auther;
    private int publishDate;
    private String publisher;
    private byte[] image;
    private String descr;
    private boolean edit;
    
    private static final String CONTENT_QUERY = "select content from book where id=";
    
    public Book() {
        
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String ganreId) {
        this.genre = ganreId;
    }

    public String getAuther() {
        return auther;
    }

    public void setAuther(String autherId) {
        this.auther = autherId;
    }

    public int getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(int publishYear) {
        this.publishDate = publishYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisherId) {
        this.publisher = publisherId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
      public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
    
    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }
    
    public void fillPdfContent() {
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            con = Database.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(CONTENT_QUERY + this.getId());
            while (rs.next()) {
                this.setContent(rs.getBytes("content"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }
}
