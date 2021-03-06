package com.library.web.controllers;

import com.library.web.beans.Book;
import com.library.web.db.Database;
import com.library.web.enums.SearchType;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import javax.faces.event.ValueChangeEvent;

@ManagedBean
@SessionScoped
public class SearchController implements Serializable{
    
    private List<Book> currentBookList; //current books list
    private List<Integer> pageNumbers = new ArrayList<>(); //number of pages for paging through
    private boolean pageSelected;
    private SearchType searchType = SearchType.TITLE; //keep search type
    private static Map<String, SearchType> searchlist = new HashMap<String, SearchType>(); 
    private String currentSearchString; //current search string   
    private int booksCountOnPage = 2; //number of books displayed on one page
    private long selectedPageNumber = 1; //default selected page
    private long totalBooksCount; //general books count
    private String currentSqlNoLimit; //lust sql query with out the limit
    private int selectedGenreId; //selected genre
    private char selectedLetter; //selected letter, no leeter is choosen by default
    private int pageCount; //page 
    //-----------------
    private boolean editModeView; //view edit mode

    public String getCurrentSqlNoLimit() {
        return currentSqlNoLimit;
    }

    public void setCurrentSqlNoLimit(String currentSqlNoLimit) {
        this.currentSqlNoLimit = currentSqlNoLimit;
    }

    
    public SearchController() {
        fillBooksAll();
        
        ResourceBundle bundle = ResourceBundle.getBundle("com.library.web.nls.messages", 
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchlist.put(bundle.getString("author_name"), searchType.AUTHOR);
        searchlist.put(bundle.getString("book_name"), searchType.TITLE);
    }
    
    //<editor-fold defaultstate="collapsed" desc="queries to database">
    private void fillBooksBySQL(String query) {
        
        StringBuilder sqlBuilder = new StringBuilder(query);
        
        currentSqlNoLimit = query;
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            
            if (!pageSelected) {
                rs = stmt.executeQuery(sqlBuilder.toString());
                rs.last();
                
                totalBooksCount = rs.getRow();
                
                fillPageNumbers(totalBooksCount, booksCountOnPage);
            }
            
            if (totalBooksCount > booksCountOnPage) {
                sqlBuilder.append(" limit ").append(selectedPageNumber * booksCountOnPage - booksCountOnPage).append(",").append(booksCountOnPage);
            }
            
            rs = stmt.executeQuery(sqlBuilder.toString());
            
            currentBookList = new ArrayList<>();
            
            System.out.println(sqlBuilder);
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setName(rs.getString("name"));
                book.setGenre(rs.getString("genre"));
                book.setIsbn(rs.getString("isbn"));
                book.setAuther(rs.getString("author"));
                book.setPageCount(rs.getInt("page_count"));
                book.setPublishDate(rs.getInt("publish_year"));
                book.setPublisher(rs.getString("publisher"));
                //book.setImage(rs.getBytes("image"));
                book.setDescr(rs.getString("descr"));
                currentBookList.add(book);
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
    
    public void fillBooksAll() {
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + " order by b.name ");
    }
    
    public void fillBooksByGenre() {
        
        imitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedGenreId = Integer.valueOf(params.get("genre_id"));
        
        submitValues(' ', 1, selectedGenreId, false);
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where genre_id=" + selectedGenreId + " order by b.name ");
    }
    
    public void fillBooksByLetter(){
        
        imitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);
        
        submitValues(selectedLetter, 1, -1, false);
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where substr(b.name,1,1)='" + selectedLetter + "' order by b.name ");
    }
    
    public void fillBooksBySearch() {
        
        imitateLoading();
        
        if (currentSearchString.trim().length() == 0) {
            fillBooksAll();
            return;
        }
        
        submitValues(' ', 1, -1, false);
        
        StringBuilder sql = new StringBuilder("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image, b.descr from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id ");
        
        if (SearchType.AUTHOR == searchType) {
            sql.append("where lower(a.fio) like '%" + currentSearchString.toLowerCase() + "%' order by b.name ");
            
        } else if (searchType == SearchType.TITLE) {
            sql.append("where lower(b.name) like '%" + currentSearchString.toLowerCase() + "%' order by b.name ");
        }
        
        fillBooksBySQL(sql.toString());
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
    
    public String updateBooks() {
        
        imitateLoading();
        
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Connection con = null;
        
        try {
            con = Database.getConnection();
            prepStmt = con.prepareStatement("update book set name=?, isbn=?, page_count=?, publish_year=?, descr=? where id=?");
            
            for (Book book : currentBookList) {
                if (!book.isEdit()) continue;
                prepStmt.setString(1, book.getName());
                prepStmt.setString(2, book.getIsbn());
                prepStmt.setInt(3, book.getPageCount());
                
                System.out.println("count - " + book.getPageCount());
                
                prepStmt.setInt(4, book.getPublishDate());
                prepStmt.setString(5, book.getDescr());
                prepStmt.setLong(6, book.getId());
                
                prepStmt.addBatch();
            }
            
            prepStmt.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (prepStmt != null) prepStmt.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        cancelEdit();
        return "books";
    }
//</editor-fold>
    
    private void submitValues(Character selectedLetter, long selectedPageNumber, int selectedGenreId, boolean requestFromPager) {
        this.selectedLetter = selectedLetter;
        this.selectedPageNumber = selectedPageNumber;
        this.selectedGenreId = selectedGenreId;
        this.pageSelected = requestFromPager;

    }
    
    public void selectPage() {
        cancelEdit();
        imitateLoading();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        pageSelected = true;
        fillBooksBySQL(currentSqlNoLimit); 
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
    
    //<editor-fold defaultstate="collapsed" desc="page by page">
    private void fillPageNumbers(long totalBooksCount, int booksOnPage) {
        
        pageCount = totalBooksCount > 0 ? (int) (totalBooksCount / booksOnPage) : 0;
        
        if (totalBooksCount / booksOnPage % booksOnPage == 0) {
            pageCount = totalBooksCount > 0 ? (int) (totalBooksCount / booksOnPage) : 0;
        } else {
            pageCount = totalBooksCount > 0 ? (int) (totalBooksCount / booksOnPage)+1 : 0;
        }
        
        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }
    }
    
    public void booksOnPageChanged(ValueChangeEvent e) {
        imitateLoading();
        
        cancelEdit();
        pageSelected = false;
        booksCountOnPage = Integer.valueOf(e.getNewValue().toString()).intValue();
        selectedPageNumber = 1;
        fillBooksBySQL(currentSqlNoLimit);
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="editing mode">
    public void cancelEdit(){
        editModeView = false;
        for (Book book : currentBookList) {
            book.setEdit(false);
        }
    }
    
    public void showEdit() {
        editModeView = true;
    }
//</editor-fold>
    
    public void changeSearchString(ValueChangeEvent e) {
        currentSearchString = e.getNewValue().toString();
    }
    
    public void changeSearchType(ValueChangeEvent e) {
        searchType = (SearchType) e.getNewValue();
    }
    
    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    public List<Book> getCurrentBookList() {
        return currentBookList;
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
    
    public String getCurrentSearchString() {
        return currentSearchString;
    }
    
    public void setCurrentSearchString(String currentSearchString) {
        this.currentSearchString = currentSearchString;
    }
    
    public long getTotalBooksCount() {
        return totalBooksCount;
    }
    
    public void setTotalBooksCount(long totalBooksCount) {
        this.totalBooksCount = totalBooksCount;
    }
    
    public int getBooksCountOnPage() {
        return booksCountOnPage;
    }
    
    public void setBooksCountOnPage(int booksCountOnPage) {
        this.booksCountOnPage = booksCountOnPage;
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
    
    public boolean isPageSelected() {
        return pageSelected;
    }
    
    public void setPageSelected(boolean pageSelected) {
        this.pageSelected = pageSelected;
    }
    
    public boolean isEditModeView() {
        return editModeView;
    }
    
    public void setEditModeView(boolean editModeView) {
        this.editModeView = editModeView;
    }
//</editor-fold>
    
    private void imitateLoading() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
