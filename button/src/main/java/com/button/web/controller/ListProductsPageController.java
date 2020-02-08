package com.button.web.controller;

import com.button.model.entity.Product;
import com.button.model.entity.ProductProperty;
import com.button.model.repo.ProductListRepository;
import com.button.model.repo.ProductPropertyRepository;
import com.button.model.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/list_products")
public class ListProductsPageController {
    @Autowired
    private ProductPropertyRepository productPropertyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductListRepository productListRepository;

    @GetMapping("/{list_id}")
    public String listProductsPage(Model model, @PathVariable("list_id") Integer listId) {
        Iterable<ProductProperty> listProducts = productPropertyRepository.findProductsByProductListId(listId);
        model.addAttribute("list_id", listId);
        model.addAttribute("listProducts", listProducts);
        return "list_products";
    }

    @GetMapping("/{list_id}/add_product")
    public String addProductPage(Model model, @PathVariable("list_id") Integer listId) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("new_product", new Product());
        model.addAttribute("list", listId);
        return "add_product";
    }

    @PostMapping("/{list_id}/add")
    public String addProductToList(@PathVariable("list_id") Integer listId,
                                   @ModelAttribute("new_product") Product newProduct)
    {
        Product product = productRepository.findProductByName(newProduct.getName());
        boolean isNewProductEqualsNothing = newProduct.getName() == null || newProduct.getName().trim().length() == 0;

        if ( ! isNewProductEqualsNothing) {
            if (product == null) {
                newProduct.setName(newProduct.getName().trim());
                product = productRepository.save(newProduct);
            }

            ProductProperty productProperty = new ProductProperty();
            productProperty.setProductId(product.getId());
            productProperty.setProductListId(listId);

            //наличие продукта в ProductList увеличивает его количество на 1, копирует остальные данные
            Iterable<ProductProperty> listProducts = productPropertyRepository.findProductsByProductListId(listId);
            for (ProductProperty productProperty1: listProducts) {
                if (productProperty1.getProduct().getId().intValue() == product.getId().intValue()) {
                    productProperty.setId(productProperty1.getId());
                    productProperty.setQuantity(productProperty1.getQuantity());
                    productProperty.setUnits(productProperty1.getUnits());
                    productProperty.setState(false);
                }
            }

            productPropertyRepository.save(productProperty);
        }
        return "redirect:/list_products/" + listId;
    }
}
