package com.library.web.controllers;

import com.library.web.beans.Book;
import com.library.web.db.Database;
import com.library.web.enums.SearchType;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@ManagedBean
@SessionScoped
public class SearchController implements Serializable{
    
    private boolean requestFromPager;
    private SearchType searchType;
    private static Map<String, SearchType> searchlist = new HashMap<String, SearchType>();
    private List<Book> bookList;
    private String searchString;    
    private int booksOnPage = 2;
    private long selectedPageNumber = 1;
    private long totalBooksCount;
    private List<Integer> pageNumbers = new ArrayList<>();
    private String currentSql;
    private int selectedGenreId;
    private char selectedLetter;
    
    public SearchController() {
        fillBooksAll();
        
        ResourceBundle bundle = ResourceBundle.getBundle("com.library.web.nls.messages", 
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchlist.put(bundle.getString("author_name"), searchType.AUTHOR);
        searchlist.put(bundle.getString("book_name"), searchType.TITLE);
    }
    
    public void fillBooksAll() {
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + " order by b.name ");
    }
    
    public void fillBooksByGenre() {
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedGenreId = Integer.valueOf(params.get("genre_id"));
        
        submitValues(' ', selectedGenreId, 1, false);
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where genre_id=" + selectedGenreId + " order by b.name ");
    }
    
    public void fillBooksByLetter(){
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);
        
        submitValues(selectedLetter, -1, 1, false);
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where substr(b.name,1,1)='" + selectedLetter + "' order by b.name ");
    }
    
    public void fillBooksBySearch() {
        
        if (searchString.trim().length() == 0) {
            fillBooksAll();
            return;
        }
        
        submitValues(' ', -1, 1, false);
        
        StringBuilder sql = new StringBuilder("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id ");
        
        if (SearchType.AUTHOR == searchType) {
            sql.append("where lower(a.fio) like '%" + searchString.toLowerCase() + "%' order by b.name ");

        } else if (searchType == SearchType.TITLE) {
            sql.append("where lower(b.name) like '%" + searchString.toLowerCase() + "%' order by b.name ");
        }
        
        fillBooksBySQL(sql.toString());
    }
    
    private void submitValues(char selectedLetter, int selectedGenreId, long selectedPageNumber , boolean requestFromPager) {
        this.selectedLetter = selectedLetter;
        this.selectedGenreId = selectedGenreId;
        this.selectedPageNumber = selectedPageNumber;
        this.requestFromPager = requestFromPager;
    }
    
    public String selectPage() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        fillBooksBySQL(currentSql);
        requestFromPager = true;
        return "books";
    }
    
    private void fillBooksBySQL(String query) {
        
        StringBuilder sqlBuilder = new StringBuilder(query);
        
        currentSql = query;
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
        conn = Database.getConnection();
        stmt = conn.createStatement();
                
        if (!requestFromPager) {
                rs = stmt.executeQuery(sqlBuilder.toString());
                rs.last();

                totalBooksCount = rs.getRow();
            
                fillPageNumbers(totalBooksCount, booksOnPage);
            }
            
            if (totalBooksCount > booksOnPage) {  
                sqlBuilder.append(" limit ").append(selectedPageNumber * booksOnPage - booksOnPage).append(",").append(booksOnPage);    
            }
            
            rs = stmt.executeQuery(sqlBuilder.toString());
            
            bookList = new ArrayList<>();
            
            System.out.println(sqlBuilder);
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setName(rs.getString("name"));
                book.setGenre(rs.getString("genre"));
                book.setIsbn(rs.getString("isbn"));
                book.setAuther(rs.getString("author"));
                book.setPageCount(rs.getInt("page_count"));
                book.setPublishDate(rs.getDate("publish_year"));
                book.setPublisher(rs.getString("publisher"));
                //book.setImage(rs.getBytes("image"));
                book.setDescr(rs.getString("descr"));
                bookList.add(book);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public byte[] getImage(int id) {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        byte[] image = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select image from book where id=" + id);
            while (rs.next()) {
                image = rs.getBytes("image");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return image;
    }
    
    public byte[] getContent(int id) {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        byte[] image = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select content from book where id=" + id);
            while (rs.next()) {
                image = rs.getBytes("content");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return image;
    }
    
    public Character[] getRussianLetters() {
        Character[] letters = new Character[33];
        letters[0] = 'А';
        letters[1] = 'Б';
        letters[2] = 'В';
        letters[3] = 'Г';
        letters[4] = 'Д';
        letters[5] = 'Е';
        letters[6] = 'Ё';
        letters[7] = 'Ж';
        letters[8] = 'З';
        letters[9] = 'И';
        letters[10] = 'Й';
        letters[11] = 'К';
        letters[12] = 'Л';
        letters[13] = 'М';
        letters[14] = 'Н';
        letters[15] = 'О';
        letters[16] = 'П';
        letters[17] = 'Р';
        letters[18] = 'С';
        letters[19] = 'Т';
        letters[20] = 'У';
        letters[21] = 'Ф';
        letters[22] = 'Х';
        letters[23] = 'Ц';
        letters[24] = 'Ч';
        letters[25] = 'Ш';
        letters[26] = 'Щ';
        letters[27] = 'Ъ';
        letters[28] = 'Ы';
        letters[29] = 'Ь';
        letters[30] = 'Э';
        letters[31] = 'Ю';
        letters[32] = 'Я';

        return letters;
    }
    
    private void fillPageNumbers(long totalBooksCount, int booksOnPage) {

        int pageCount = totalBooksCount > 0 ? (int) (totalBooksCount / booksOnPage) : 0;
        
        if (totalBooksCount / booksOnPage % booksOnPage > 0) {
            pageCount++;
        }
        
        System.out.println(pageCount);

        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }
    }
    
     public List<Book> getBookList() {
        return bookList;
    }
    
     public SearchType getSearchType() {
        return searchType;
    }
     
    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public Map<String, SearchType> getSearchlist() {
        return searchlist;
    }
    
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
    
    public long getTotalBooksCount() {
        return totalBooksCount;
    }

    public void setTotalBooksCount(long totalBooksCount) {
        this.totalBooksCount = totalBooksCount;
    }
    
    public int getBooksOnPage() {
        return booksOnPage;
    }
    
    public void setBooksOnPage(int booksOnPage) {
        this.booksOnPage = booksOnPage;
    }
    
    public List<Integer> getPageNumbers() {
        return pageNumbers;
    }

    public void setPageNumbers(List<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }
    
    public long getSelectedPageNumber() {
        return selectedPageNumber;
    }

    public void setSelectedPageNumber(long selectedPageNumber) {
        this.selectedPageNumber = selectedPageNumber;
    }
    
    public int getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public char getSelectedLetter() {
        return selectedLetter;
    }

    public void setSelectedLetter(char selectedLetter) {
        this.selectedLetter = selectedLetter;
    }
    
    public boolean isRequestFromPager() {
        return requestFromPager;
    }

    public void setRequestFromPager(boolean requestFromPager) {
        this.requestFromPager = requestFromPager;
    }
}
