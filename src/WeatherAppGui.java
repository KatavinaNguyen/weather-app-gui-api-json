import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui() {
        super("Local Weather Broadcast - CSNBCWE34");

        // Frame setup
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(445, 630);
        setLocationRelativeTo(null);
        setResizable(false);

        // Gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        addGuiComponents();
    }

    private void addGuiComponents() {
        // Search input field
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(20, 25, 330, 45);
        searchTextField.setFont(new Font("SansSerif", Font.PLAIN, 20));
        add(searchTextField);

        // Search button with icon
        JButton searchButton = new JButton(loadImage("src/assets/search.png", 45, 45));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(360, 25, 45, 45);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(false);
        add(searchButton);

        // Weather icon display
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png", 160, 160));
        weatherConditionImage.setBounds(145, 90, 160, 160);
        add(weatherConditionImage);

        // Temperature display
        JLabel temperatureText = new JLabel("75°F");
        temperatureText.setBounds(0, 270, 450, 54);
        temperatureText.setFont(new Font("SansSerif", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        temperatureText.setForeground(Color.WHITE);
        add(temperatureText);

        // Weather description
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 330, 450, 36);
        weatherConditionDesc.setFont(new Font("SansSerif", Font.PLAIN, 28));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        weatherConditionDesc.setForeground(Color.WHITE);
        add(weatherConditionDesc);

        // Panel for humidity and wind speed info
        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(15, 400, 400, 170);
        infoPanel.setLayout(new GridLayout(1, 2, 0, 0));
        add(infoPanel);

        // Humidity display panel
        JPanel humidityPanel = createInfoPanel("src/assets/humidity.png", "Humidity", "70%", Color.WHITE);
        infoPanel.add(humidityPanel);

        // Wind speed display panel
        JPanel windspeedPanel = createInfoPanel("src/assets/windspeed.png", "Wind", "4 mph", Color.WHITE);
        infoPanel.add(windspeedPanel);

        // Search button action to fetch and display weather data
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText().trim();
                if (userInput.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid city name.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Get and update weather data
                weatherData = WeatherApp.getWeatherData(userInput);
                if (weatherData != null) {
                    updateWeatherDisplay(weatherData, weatherConditionImage, temperatureText, weatherConditionDesc, humidityPanel, windspeedPanel);
                } else {
                    JOptionPane.showMessageDialog(null, "No weather data found for this location.", "Data Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Creates a panel with icon, label, and value for display
    private JPanel createInfoPanel(String iconPath, String label, String value, Color textColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 15));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel(loadImage(iconPath, 60, 60));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel labelText = new JLabel("<html><b>" + label + "</b></html>");
        labelText.setHorizontalAlignment(SwingConstants.CENTER);
        labelText.setFont(new Font("SansSerif", Font.PLAIN, 18));
        labelText.setForeground(textColor);

        JLabel valueText = new JLabel(value);
        valueText.setHorizontalAlignment(SwingConstants.CENTER);
        valueText.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueText.setForeground(Color.WHITE);

        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(labelText, BorderLayout.CENTER);
        panel.add(valueText, BorderLayout.SOUTH);

        return panel;
    }

    // Updates display elements with new weather data
    private void updateWeatherDisplay(JSONObject data, JLabel imageLabel, JLabel tempLabel, JLabel conditionLabel, JPanel humidityPanel, JPanel windspeedPanel) {
        // Update weather icon based on condition
        String condition = (String) data.get("weather_condition");
        switch (condition) {
            case "Clear":
                imageLabel.setIcon(loadImage("src/assets/clear.png", 160, 160));
                break;
            case "Cloudy":
                imageLabel.setIcon(loadImage("src/assets/cloudy.png", 160, 160));
                break;
            case "Rain":
                imageLabel.setIcon(loadImage("src/assets/rainy.png", 160, 160));
                break;
            case "Snow":
                imageLabel.setIcon(loadImage("src/assets/snowy.png", 160, 160));
                break;
        }

        // Update temperature, condition, humidity, and wind speed
        double temperature = (double) data.get("temperature");
        tempLabel.setText((int) temperature + "°F");
        conditionLabel.setText(condition);

        long humidity = (long) data.get("humidity");
        double windSpeed = (double) data.get("windspeed");

        ((JLabel) humidityPanel.getComponent(2)).setText(humidity + "%");
        ((JLabel) windspeedPanel.getComponent(2)).setText(windSpeed + " mph");
    }

    // Loads and resizes image for icons
    private ImageIcon loadImage(String path, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find resource");
        return null;
    }

    // Gradient background for main panel
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            Color color1 = new Color(50, 50, 50);
            Color color2 = new Color(0, 0, 0);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
