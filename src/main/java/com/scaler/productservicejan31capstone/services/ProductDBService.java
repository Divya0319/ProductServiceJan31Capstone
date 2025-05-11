package com.scaler.productservicejan31capstone.services;

import com.scaler.productservicejan31capstone.exceptions.ProductNotFoundException;
import com.scaler.productservicejan31capstone.models.Category;
import com.scaler.productservicejan31capstone.models.Product;
import com.scaler.productservicejan31capstone.repositories.CategoryRepository;
import com.scaler.productservicejan31capstone.repositories.ProductRepository;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("productDBService")
public class ProductDBService implements ProductService, ProductAIService
{

    private final ChatClient chatClient;
    private final AzureOpenAiChatModel azureOpenAiChatModel;
    private final ImageGenerationAIService imageGenerationAIService;
    ProductRepository productRepository;
    CategoryRepository categoryRepository;

    public ProductDBService(ProductRepository productRepository,
                            CategoryRepository categoryRepository, ChatClient chatClient, AzureOpenAiChatModel azureOpenAiChatModel, ImageGenerationAIService imageGenerationAIService)
    {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.chatClient = chatClient;
        this.azureOpenAiChatModel = azureOpenAiChatModel;
        this.imageGenerationAIService = imageGenerationAIService;
    }

    @Override
    public Product getProductById(long id) throws ProductNotFoundException
    {

        Optional<Product> optionalProduct = productRepository.findById(id);

        if(optionalProduct.isEmpty())
        {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }

        return optionalProduct.get();
    }

    @Override
    public List<Product> getAllProducts()
    {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(String name, String description, double price,
                                 String imageUrl, String category)
    {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        Category categoryObj = getCategoryFromDB(category);

        product.setCategory(categoryObj);
        return productRepository.save(product);
    }

    private Category getCategoryFromDB(String name)
    {
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        if(optionalCategory.isPresent())
        {
            return optionalCategory.get();
        }

        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    @Override
    public Product createProductWithAIDescription(String name, double price, String imageUrl, String category)
    {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        Category categoryObj = getCategoryFromDB(category);
        product.setCategory(categoryObj);

        String description = getDescriptionFromAI(product);

        if(imageUrl == null)
            imageUrl = getImageURLFromAI(description);

        System.out.println(imageUrl);

        product.setDescription(description);

        return productRepository.save(product);
    }

    private String getDescriptionFromAI(Product product)
    {

//        return chatClient.prompt().user(prompt).call().content();

        String message = """
                        Generate a 150-word professional marketing description for a {categoryInLowercase} product named '{productName}'.
                      Key features: Priced at {price}, Category: {category}.
                      Focus on benefits and unique selling points. Avoid technical jargon. Use markdown formatting.
                """;

        Map<String, Object> map = new HashMap<>();
        map.put("categoryInLowercase", product.getCategory().getName().toLowerCase());
        map.put("productName", product.getName());
        map.put("price", Double.toString(product.getPrice()));
        map.put("category", product.getCategory().getName());

        PromptTemplate promptTemplate = new PromptTemplate(message, map);
        Prompt prompt = promptTemplate.create();
        ChatResponse chatResponse = azureOpenAiChatModel.call(prompt);
        return chatResponse.getResults().get(0).getOutput().getText();

    }

    private String getImageURLFromAI(String description) {
        return imageGenerationAIService.generateImageUrl(description);
    }
}
