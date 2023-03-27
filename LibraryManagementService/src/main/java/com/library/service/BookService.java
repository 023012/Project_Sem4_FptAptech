package com.library.service;

import com.library.entity.Book;

import java.util.List;

public interface BookService {
    Book createBook(Book book);

    List<Book> getAllBooks();

    List<Book> getAllBookByCategoryID(Long cateID);

    String deleteBook(Long id);

    Book updateBook(Long id, Book book);

    List<Book> getAllBookByKeyword(String keyword);

    List<Book> getAllBookByCateIDAndKeyword(Long cateID, String keyword);
}