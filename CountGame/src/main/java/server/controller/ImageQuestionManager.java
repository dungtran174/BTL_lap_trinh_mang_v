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
    
    private static ImageQuestionManager instance;
    private Map<String, ImageQuestion> imageQuestions;
    private List<String> imagePaths;
    private Random random;
    
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
        // Temporary question generation - user will update later
        // For now, assign random questions based on image number
        if (imageName.contains("image1")) {
            return "Có bao nhiêu con sứa?";
        } else if (imageName.contains("image2")) {
            return "Có bao nhiêu quả cà chua?";
        } else if (imageName.contains("image3")) {
            return "Có bao nhiêu cái kèn?";
        } else if (imageName.contains("image4")) {
            return "Có bao nhiêu con bọ rùa?";
        } else if (imageName.contains("image5")) {
            return "Có bao nhiêu con vịt cổ xanh?";
        } else if (imageName.contains("image6")) {
            return "Có bao nhiêu cái pizza?";
        } else if (imageName.contains("image7")) {
            return "Có bao nhiêu con gấu bông?";
        } else if (imageName.contains("image8")) {
            return "Có bao nhiêu con cá sấu?";
        } else if (imageName.contains("image9")) {
            return "Có bao nhiêu cái bút lông vẽ?";
        } else if (imageName.contains("image10")) {
            return "Có bao nhiêu xe cứu hỏa?";
        } else if (imageName.contains("image11")) {
            return "Có bao nhiêu quả dâu tây?";
        } else {
            return "Có bao nhiêu vật thể trong hình?";
        }
    }
    
    private int generateAnswer(String imageName) {
        // Temporary answer generation - user will update later
        // For now, assign random answers based on image number
        if (imageName.contains("image1")) {
            return 9;
        } else if (imageName.contains("image2")) {
            return 9;
        } else if (imageName.contains("image3")) {
            return 3;
        } else if (imageName.contains("image4")) {
            return 5;
        } else if (imageName.contains("image5")) {
            return 3;
        } else if (imageName.contains("image6")) {
            return 5;
        } else if (imageName.contains("image7")) {
            return 4;
        } else if (imageName.contains("image8")) {
            return 5;
        } else if (imageName.contains("image9")) {
            return 9;
        } else if (imageName.contains("image10")) {
            return 6;
        } else if (imageName.contains("image11")) {
            return 2;
        } else {
            return random.nextInt(10) + 1;
        }
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

