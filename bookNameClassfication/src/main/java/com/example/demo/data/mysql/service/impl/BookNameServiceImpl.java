package com.example.demo.data.mysql.service.impl;

import com.example.demo.data.mysql.mapper.BookNameMapper;
import com.example.demo.data.mysql.service.BookNameService;
import com.example.demo.pojo.sql.Book;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BookNameServiceImpl implements BookNameService {
    @Resource
    private BookNameMapper bookNameMapper;

    @Override
    public int addBookName(String bookName) {
        Book book = new Book(bookName);
        bookNameMapper.insert(book);
        return book.getBookId();
    }
}
