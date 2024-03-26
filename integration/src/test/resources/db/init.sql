INSERT INTO category(category_id, name, is_visible, parent_category_id) VALUE (1, "clothes", 1, null);

INSERT INTO product(product_id, uuid, name, description, default_image_key, weight, weight_type, category_id)
    VALUE (1, uuid(), "product-1", "lorem 5000", "image-key", 2.5, "kg", 1);