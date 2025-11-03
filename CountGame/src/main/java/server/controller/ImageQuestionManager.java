package server.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ImageQuestionManager {
    
    // Class helper để lưu cặp câu hỏi - đáp án
    private static class QuestionAnswerPair {
        String question;
        int answer;
        
        QuestionAnswerPair(String question, int answer) {
            this.question = question;
            this.answer = answer;
        }
    }
    
    private static ImageQuestionManager instance;
    private Map<String, ImageQuestion> imageQuestions;
    private List<String> imagePaths;
    private Random random;
    
    // Map lưu câu hỏi và đáp án theo tên file (không cần regex)
    private static final Map<String, QuestionAnswerPair> IMAGE_DATA = new HashMap<>();
    
    static {
        // Chỉ cần ghi tên file (không có đường dẫn, có phần mở rộng)
        IMAGE_DATA.put("image1.jpg", new QuestionAnswerPair("Có bao nhiêu con sứa?", 9));
        IMAGE_DATA.put("image2.jpg", new QuestionAnswerPair("Có bao nhiêu quả cà chua?", 9));
        IMAGE_DATA.put("image3.jpg", new QuestionAnswerPair("Có bao nhiêu cái kèn?", 3));
        IMAGE_DATA.put("image4.jpg", new QuestionAnswerPair("Có bao nhiêu con bọ rùa?", 5));
        IMAGE_DATA.put("image5.jpg", new QuestionAnswerPair("Có bao nhiêu con vịt cổ xanh?", 3));
        IMAGE_DATA.put("image6.jpg", new QuestionAnswerPair("Có bao nhiêu cái pizza?", 5));
        IMAGE_DATA.put("image7.jpg", new QuestionAnswerPair("Có bao nhiêu con gấu bông?", 4));
        IMAGE_DATA.put("image8.jpg", new QuestionAnswerPair("Có bao nhiêu con cá sấu?", 5));
        IMAGE_DATA.put("image9.jpg", new QuestionAnswerPair("Có bao nhiêu cái bút lông vẽ?", 9));
        IMAGE_DATA.put("image10.jpg", new QuestionAnswerPair("Có bao nhiêu xe cứu hỏa?", 6));
        IMAGE_DATA.put("image11.jpg", new QuestionAnswerPair("Có bao nhiêu quả dâu tây?", 2));
        
        // Hỗ trợ thêm .png nếu bạn có ảnh PNG
        IMAGE_DATA.put("image1.png", new QuestionAnswerPair("Có bao nhiêu con sứa?", 9));
        IMAGE_DATA.put("image2.png", new QuestionAnswerPair("Có bao nhiêu quả cà chua?", 9));
        IMAGE_DATA.put("image3.png", new QuestionAnswerPair("Có bao nhiêu cái kèn?", 3));
        IMAGE_DATA.put("image4.png", new QuestionAnswerPair("Có bao nhiêu con bọ rùa?", 5));
        IMAGE_DATA.put("image5.png", new QuestionAnswerPair("Có bao nhiêu con vịt cổ xanh?", 3));
        IMAGE_DATA.put("image6.png", new QuestionAnswerPair("Có bao nhiêu cái pizza?", 5));
        IMAGE_DATA.put("image7.png", new QuestionAnswerPair("Có bao nhiêu con gấu bông?", 4));
        IMAGE_DATA.put("image8.png", new QuestionAnswerPair("Có bao nhiêu con cá sấu?", 5));
        IMAGE_DATA.put("image9.png", new QuestionAnswerPair("Có bao nhiêu cái bút lông vẽ?", 9));
        IMAGE_DATA.put("image10.png", new QuestionAnswerPair("Có bao nhiêu xe cứu hỏa?", 6));
        IMAGE_DATA.put("image11.png", new QuestionAnswerPair("Có bao nhiêu quả dâu tây?", 2));
    }
    
    private ImageQuestionManager() {
        imageQuestions = new HashMap<>();
        imagePaths = new ArrayList<>();
        random = new Random();
        loadImages();
    }
    
    public static ImageQuestionManager getInstance() {
        if (instance == null) {
            instance = new ImageQuestionManager();
        }
        return instance;
    }
    
    private void loadImages() {
        // Load images from src/main/resources/Images/img folder
        // Try to get the path relative to classpath first
        String basePath = "Images/img";
        File imgFolder = new File("src/main/resources/" + basePath);
        
        // If that doesn't work, try absolute path
        if (!imgFolder.exists()) {
            imgFolder = new File(basePath);
        }
        
        if (!imgFolder.exists() || !imgFolder.isDirectory()) {
            System.err.println("Image folder not found: " + basePath);
            return;
        }
        
        File[] files = imgFolder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".png") ||
            name.toLowerCase().endsWith(".jpeg"));
        
        if (files == null) {
            System.err.println("No image files found in: " + basePath);
            return;
        }
        
        for (File file : files) {
            String imageName = file.getName();
            // Store the absolute path for loading the image
            String imagePath = file.getAbsolutePath();
            
            // Create a question and answer for each image (temporary, user will update later)
            String question = generateQuestion(imageName);
            int answer = generateAnswer(imageName);
            
            imageQuestions.put(imagePath, new ImageQuestion(imagePath, question, answer));
            imagePaths.add(imagePath);
            
            System.out.println("Loaded image: " + imagePath + " - Q: " + question + " - A: " + answer);
        }
        
        System.out.println("Total images loaded: " + imagePaths.size());
    }
    
    private String generateQuestion(String imageName) {
        // Tìm trong Map theo tên file trực tiếp
        QuestionAnswerPair data = IMAGE_DATA.get(imageName.toLowerCase());
        if (data != null) {
            return data.question;
        }
        // Nếu không tìm thấy, trả về câu hỏi mặc định
        return "Có bao nhiêu vật thể trong hình?";
    }
    
    private int generateAnswer(String imageName) {
        // Tìm trong Map theo tên file trực tiếp
        QuestionAnswerPair data = IMAGE_DATA.get(imageName.toLowerCase());
        if (data != null) {
            return data.answer;
        }
        // Nếu không tìm thấy, trả về đáp án ngẫu nhiên
        return random.nextInt(10) + 1;
    }
    
    public ImageQuestion getRandomImageQuestion() {
        if (imagePaths.isEmpty()) {
            return null;
        }
        String randomPath = imagePaths.get(random.nextInt(imagePaths.size()));
        return imageQuestions.get(randomPath);
    }
    
    public List<ImageQuestion> getRandomImageQuestions(int count) {
        List<ImageQuestion> selected = new ArrayList<>();
        List<String> availablePaths = new ArrayList<>(imagePaths);
        
        for (int i = 0; i < count && !availablePaths.isEmpty(); i++) {
            int index = random.nextInt(availablePaths.size());
            String selectedPath = availablePaths.remove(index);
            selected.add(imageQuestions.get(selectedPath));
        }
        
        return selected;
    }
    
    public byte[] getImageBytes(String imagePath) {
        try {
            return Files.readAllBytes(Paths.get(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static class ImageQuestion {
        private String imagePath;
        private String question;
        private int answer;
        
        public ImageQuestion(String imagePath, String question, int answer) {
            this.imagePath = imagePath;
            this.question = question;
            this.answer = answer;
        }
        
        public String getImagePath() {
            return imagePath;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public int getAnswer() {
            return answer;
        }
        
        public byte[] getImageBytes() {
            try {
                return Files.readAllBytes(Paths.get(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

