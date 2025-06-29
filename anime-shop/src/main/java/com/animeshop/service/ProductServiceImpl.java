package com.animeshop.service;

import com.animeshop.entity.Product;
import com.animeshop.repository.OrderItemRepository;
import com.animeshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final OrderItemRepository orderItemRepository;

    public ProductServiceImpl(ProductRepository repo,
                              OrderItemRepository orderItemRepository) {
        this.repo = repo;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<Product> findAll() {
        return repo.findAll();
    }

    @Override
    public Product findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Product save(Product product) {
        return repo.save(product);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return repo.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findByPriceRange(Double min, Double max) {
        return repo.findByPriceBetween(
                min != null ? min : 0.0,
                max != null ? max : Double.MAX_VALUE
        );
    }

    @Override
    public List<Product> findByCategoryAndPrice(Long categoryId, Double min, Double max) {
        return repo.findByCategoryIdAndPriceBetween(
                categoryId,
                min != null ? min : 0.0,
                max != null ? max : Double.MAX_VALUE
        );
    }

    @Override
    public List<Product> searchByName(String query) {
        return repo.searchByName(query);
    }

    @Override
    public List<Product> findAllById(Iterable<Long> ids) {
        return repo.findAllById(ids);
    }

    // Рейтинг (от лучших)
    @Override
    public List<Product> findAllSortedByRatingDesc() {
        return repo.findAllOrderByAverageRatingDesc();
    }

    // Рейтинг (от худших)
    @Override
    public List<Product> findAllSortedByRatingAsc() {
        return repo.findAllOrderByAverageRatingAsc();
    }

    // С фильтрами и сортировка по убыванию
    @Override
    public List<Product> findByFiltersSortedByRatingDesc(Long categoryId, Double min, Double max) {
        return repo.findByFiltersSortedByRatingDesc(categoryId, min, max);
    }

    // С фильтрами и сортировка по возрастанию
    @Override
    public List<Product> findByFiltersSortedByRatingAsc(Long categoryId, Double min, Double max) {
        return repo.findByFiltersSortedByRatingAsc(categoryId, min, max);
    }

    //Отмена заказов
    @Override
    public double getCancelRateForProduct(Long productId) {
        long total = orderItemRepository.countByProductId(productId);
        if (total == 0) return 0.0;

        long cancelled = orderItemRepository.countCancelledByProductId(productId);
        return (double) cancelled / total * 100;
    }
}
