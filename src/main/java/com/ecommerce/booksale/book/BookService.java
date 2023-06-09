package com.ecommerce.booksale.book;



import com.ecommerce.booksale.book.category.Category;
import com.ecommerce.booksale.book.category.CategoryRepository;
import com.ecommerce.booksale.book.category.CategoryService;
import com.ecommerce.booksale.book.subcategory.SubCategory;
import com.ecommerce.booksale.book.subcategory.SubCategoryService;
import com.ecommerce.booksale.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;

    // get All books
    public List<Book> findAllBooks(){
        return bookRepository.findAll();
    }

    //
    public List<Book> findTopBooks(){

        Page<Book> pageBooks = bookRepository.findAll(PageRequest.of(0, 5));

        return pageBooks.getContent();
    }


    public List<BookDTO> getBookByCategoryIdWithPaging(int id, int pageNumber, int quantity){
        Pageable pageable = PageRequest.of(pageNumber, quantity);

        Category category = categoryService.getCategoryById(id);
        Page<Book> books =  bookRepository.findByCategories(category, pageable);

        List<BookDTO> bookData = books.stream().map(BookMapper::toDTO).collect(Collectors.toList());

        return bookData;
    }


    public List<BookDTO> getBookBySubCategoryId(int id, Pageable pageable){

        SubCategory subcategory = subCategoryService.getSubcategoryById(id);

        List<Book> books = bookRepository.findBySubcategories(subcategory, pageable);

        // throw exception
        if (books.isEmpty() || books == null){
            throw new NotFoundException("Mục sản phẩm đang trống");
        }

        List<BookDTO> dataBooks = books.stream().map(BookMapper::toDTO).collect(Collectors.toList());

        return dataBooks;
    }


    // this function will add data to the model, include three list of books: fiction book, children book and self-help books
    // this data will use for home page of the website
    public void getHomeBookData(Model model){

        Map<String, Category> categoryMap = categoryService.getHomeCategories();

        model.addAttribute("categoriesMap", categoryMap);

        Pageable pageable = PageRequest.of(0, 7);

        List<Book> books = null;
        List<BookDTO> bookData = null;

        for (String category : categoryMap.keySet()){
            books = bookRepository.findBySubcategories(categoryMap.get(category)
                    .getSubCategories().get(0)
                    , pageable);

            bookData = books.stream().map(BookMapper::toDTO).collect(Collectors.toList());

            model.addAttribute(category+"Books", bookData);

        }

    }

    public BookPaging getPagingBooks(Category category, int currentPage, int itemsPerPage ){


        Pageable pageable = PageRequest.of(currentPage, itemsPerPage);
        Page<Book> bookPaging = bookRepository.findByCategories(category, pageable);

        List<Book> booksRaw = bookPaging.getContent();

        // convert List<Book> to List<BookDTO>
        List<BookDTO> bookDTOList = booksRaw.stream().map(BookMapper::toDTO).collect(Collectors.toList());

       BookPaging bookPagingData = new BookPaging();

       // set property
       bookPagingData.setBooks(bookDTOList);
       bookPagingData.setTotalPages(bookPaging.getTotalPages());
       bookPagingData.setCurrentPage(bookPaging.getNumber());
       bookPagingData.setNumberOfBooks(bookPaging.getNumberOfElements());

       return bookPagingData;
    }


}
