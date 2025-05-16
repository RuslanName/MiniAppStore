package mainFiles.controllers;

import mainFiles.configs.BotConfig;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.user.UsersRepository;
import mainFiles.database.tables.userCart.UserCart;
import mainFiles.database.tables.userCart.UserCartsRepository;
import mainFiles.database.tables.userOrder.UserOrder;
import mainFiles.database.tables.userOrder.UserOrdersRepository;
import mainFiles.database.tables.userOrderRegistration.UserOrderRegistration;
import mainFiles.database.tables.userOrderRegistration.UserOrdersRegistrationRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activate_bot")
public class BotCallbackController {

    private static final Logger log = LoggerFactory.getLogger(BotCallbackController.class);

    @Autowired
    private BotConfig config;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private UserOrdersRegistrationRepository userOrdersRegistrationRepository;

    private final String BOT_API_URL;

    public BotCallbackController(BotConfig config) {
        this.BOT_API_URL = "https://api.telegram.org/bot" + config.getToken();
    }

    @PostMapping
    public void activateBot(@RequestBody Map<String, Object> requestBody) {
        long chatId = Long.parseLong(requestBody.get("chatId").toString());
        int action = Integer.parseInt(requestBody.get("action").toString());

        if (action == 1) {
            userOrdersRegistrationRepository.deleteByChatId(chatId);

            var userCarts = userCartsRepository.findByChatIdAndSelected(chatId, true);

            for (UserCart userCart : userCarts) {
                UserOrderRegistration userOrderRegistration = new UserOrderRegistration();
                Product product = productsRepository.findById(userCart.getProductId()).get();

                int currentProductQuantity = product.getQuantity() - userCart.getQuantity();

                userOrderRegistration.setChatId(chatId);
                userOrderRegistration.setProductId(userCart.getProductId());

                if (currentProductQuantity >= 0) {
                    userOrderRegistration.setQuantity(userCart.getQuantity());
                }

                else {
                    currentProductQuantity = 0;

                    userOrderRegistration.setQuantity(product.getQuantity());
                }

                userOrdersRegistrationRepository.save(userOrderRegistration);

                product.setQuantity(currentProductQuantity);

                if (currentProductQuantity == 0) {
                    product.setVisibility(false);
                }

                productsRepository.save(product);
            }
        }

        else if (action == 2) {
            var userOrdersRegistration = userOrdersRegistrationRepository.findByChatId(chatId);

            int totalPrice = 0;
            StringBuilder description = new StringBuilder();

            for (UserOrderRegistration userOrderRegistration : userOrdersRegistration) {
                int productId = userOrderRegistration.getProductId();
                Product product = productsRepository.findById(productId).get();

                totalPrice += product.getPrice() * userOrderRegistration.getQuantity();

                if (!description.isEmpty()) {
                    description.append(", ");
                }
                description.append(product.getName()).append(" - ").append(userOrderRegistration.getQuantity());
            }

            sendInvoice(chatId, "Заказы", totalPrice, description.toString());
        }

        else if (action == 3) {
            var userOrderRegistrations = userOrdersRegistrationRepository.findByChatId(chatId);

            for (UserOrderRegistration userOrderRegistration : userOrderRegistrations) {
                Product product = productsRepository.findById(userOrderRegistration.getProductId()).get();

                if (product.getQuantity() == 0) {
                    product.setVisibility(true);
                }

                product.setQuantity(product.getQuantity() + userOrderRegistration.getQuantity());

                productsRepository.save(product);
            }

            userOrdersRegistrationRepository.deleteByChatId(chatId);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
    }

    private void sendInvoice(Long chatId, String title, int price, String description) {
        String url = BOT_API_URL + "/sendInvoice";

        JSONObject invoiceData = new JSONObject();
        invoiceData.put("chat_id", chatId);
        invoiceData.put("title", title);
        invoiceData.put("payload", "unique_invoice_payload");
        invoiceData.put("provider_token", config.getProviderToken());
        invoiceData.put("currency", "RUB");
        invoiceData.put("prices", new JSONArray()
                .put(new JSONObject()
                        .put("label", "Товар")
                        .put("amount", price * 100)
                )
        );
        invoiceData.put("description", description);
        invoiceData.put("start_parameter", "start_parameter");

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = invoiceData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                log.error("Error sending invoice. Response code: {}. Response: {}", responseCode, response.toString());
            }

        } catch (IOException e) {
            log.error("Error sending invoice: {}", e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String message) {
        String url = BOT_API_URL + "/sendMessage?chat_id=" + chatId + "&text=" + message;

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}