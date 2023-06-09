//package com.library.controller;
//
//import com.library.entity.Book;
//import com.library.entity.Order;
//import com.library.entity.OrderItem;
//import com.library.repository.BookRepository;
//import com.library.repository.OrderItemRepository;
//import com.library.repository.OrderRepository;
//import com.library.service.OrderItemService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Calendar;
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class OrderItemController {
//    private final OrderItemService orderItemService;
//    private final OrderItemRepository orderItemRepository;
//    private final OrderRepository orderRepository;
//    private final BookRepository bookRepository;
//
//    @GetMapping("/order_items")
//    public List<OrderItem> getAllOrderItems() {
//        return orderItemService.getAllOrderItems();
//    }
//
//    @GetMapping("/order_items/order")
//    public List<OrderItem> getOrderItemsByOrderID(@RequestParam("orderID") String orderID){
//        return orderItemService.getListOrderItemByOrderID(orderID);
//    }
//
//    @GetMapping("/order_items/user")
//    public ResponseEntity<?> getOrderItemListByUserID(@RequestParam("userID") Long userID) {
//        try{
//            return ResponseEntity.ok().body(orderItemService.getListOrderItemByUserID(userID));
//        } catch (NullPointerException ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//
//    @PostMapping("/order_items/add")
//    public ResponseEntity<?> createOrderItem(@RequestParam("orderId") String orderId ,
//                                             @RequestParam("bookId") Long bookId,
//                                             @RequestBody OrderItem orderItem) {
//
//        Order orderFind = orderRepository.findById(orderId).get();
//        orderItem.setOrder(orderFind);
//        Book bookFind = bookRepository.findById(bookId).get();
//        orderItem.setBook(bookFind);
//
//        //Ngay muon
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(orderItem.getBorrowedAt());
//        //int borrowTime = cal.get(Calendar.DAY_OF_MONTH);
//        long borrowTime = cal.getTimeInMillis();
//        //Ngay tra
//        Calendar cal1 = Calendar.getInstance();
//        cal1.setTime(orderItem.getReturnedAt());
//        //int returnTime = cal1.get(Calendar.DAY_OF_MONTH);
//        long returnTime = cal1.getTimeInMillis();
//
//        //long day_rangel = (returnTime - borrowTime)/(1000 * 60 * 60 * 24);
//        int day_range = Integer.parseInt(String.valueOf((returnTime - borrowTime)/(1000 * 60 * 60 * 24)));
//
//        if(orderItem.getQuantity() >= 10) {
//            return ResponseEntity.badRequest().body("Cannot borrow over 10 book items");
//        }else if(orderItem.getQuantity() >= bookFind.getAmount()){
//            return ResponseEntity.badRequest().body("Store doesn't have enough book! Please decrease your Borrow Book!");
//        }else{
//            //Update lại số lượng sách tồn kho
//            bookFind.setAmount(bookFind.getAmount() - orderItem.getQuantity());
//            bookRepository.save(bookFind);
//            //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//            orderFind.setTotalDeposit(orderFind.getTotalDeposit() + orderItem.getQuantity()*bookFind.getPrice());
//            orderFind.setTotalRent(orderFind.getTotalRent() + orderItem.getQuantity()*bookFind.getBorrowPrice()
//                    *(day_range));
//            orderRepository.save(orderFind);
//
//            System.out.println(orderItem);
//            return ResponseEntity.ok().body(orderItemService.createOrderItem(orderItem));
//        }
//    }
//
//    @PostMapping("/order_items/add-buy")
//    public ResponseEntity<?> createOrderItemWhenBuying(@RequestParam("orderId") String orderId ,
//                                                       @RequestParam("bookId") Long bookId,
//                                                       @RequestBody OrderItem orderItem) {
//
//        Order orderFind = orderRepository.findById(orderId).get();
//        orderItem.setOrder(orderFind);
//        Book bookFind = bookRepository.findById(bookId).get();
//        orderItem.setBook(bookFind);
//
//        if(orderItem.getQuantity() >= bookFind.getAmount()){
//            return ResponseEntity.badRequest().body("Store doesn't have enough books! Please decrease your amount of Book!");
//        }else{
//            //Update lại số lượng sách tồn kho
//            bookFind.setAmount(bookFind.getAmount() - orderItem.getQuantity());
//            bookRepository.save(bookFind);
//
//            //Update tổng tiền cọc - depositTotal (la so tien phai thanh toan ) trong Order
//            orderFind.setTotalDeposit(orderFind.getTotalDeposit() + orderItem.getQuantity()*bookFind.getPrice());
//            orderRepository.save(orderFind);
//
//            System.out.println(orderItem);
//            return ResponseEntity.ok().body(orderItemService.createOrderItem(orderItem));
//        }
//    }
//
//    @DeleteMapping("/order_items/delete/{id}")
//    public ResponseEntity<?> deleteOrderItem(@PathVariable Long id) {
//        OrderItem orderItemExisted = orderItemRepository.findById(id).get();
//        Book bookFind = bookRepository.findById(orderItemExisted.getBook().getId()).get();
//        Order orderFind = orderRepository.findById(orderItemExisted.getOrder().getOrderId()).get();
//
//        //Ngay muon
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(orderItemExisted.getBorrowedAt());
//        long borrowTime = cal.getTimeInMillis();
//        //Ngay tra
//        Calendar cal1 = Calendar.getInstance();
//        cal1.setTime(orderItemExisted.getReturnedAt());
//        long returnTime = cal1.getTimeInMillis();
//
//        int day_range = Integer.parseInt(String.valueOf((returnTime - borrowTime)/(1000 * 60 * 60 * 24)));
//
//        //Update lại số lượng sách tồn kho
//        bookFind.setAmount(bookFind.getAmount() + orderItemExisted.getQuantity());
//        bookRepository.save(bookFind);
//
//        //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//        orderFind.setTotalDeposit(orderFind.getTotalDeposit() - orderItemExisted.getQuantity()*bookFind.getPrice());
//        orderFind.setTotalRent(orderFind.getTotalRent() - orderItemExisted.getQuantity()*bookFind.getBorrowPrice()*(day_range));
//        orderRepository.save(orderFind);
//        return ResponseEntity.ok(orderItemService.deleteOrderItem(id));
//    }
//
//    @DeleteMapping("/order_items/delete-buy/{id}")
//    public ResponseEntity<?> deleteOrderItemWhenBuying(@PathVariable Long id) {
//        OrderItem orderItemExisted = orderItemRepository.findById(id).get();
//        Book bookFind = bookRepository.findById(orderItemExisted.getBook().getId()).get();
//        Order orderFind = orderRepository.findById(orderItemExisted.getOrder().getOrderId()).get();
//
//        //Update lại số lượng sách tồn kho
//        bookFind.setAmount(bookFind.getAmount() + orderItemExisted.getQuantity());
//        bookRepository.save(bookFind);
//
//        //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//        orderFind.setTotalDeposit(orderFind.getTotalDeposit() - orderItemExisted.getQuantity()*bookFind.getPrice());
//        orderRepository.save(orderFind);
//        return ResponseEntity.ok(orderItemService.deleteOrderItem(id));
//    }
//
//    @PutMapping("/order_items/save")
//    public ResponseEntity<?> updateOrderItem(@RequestParam("order_itemID") Long order_itemID ,
//                                             @RequestBody OrderItem orderItem) {
//        OrderItem orderItemExisted = orderItemRepository.findById(order_itemID).get();
//        Book bookFind = bookRepository.findById(orderItemExisted.getBook().getId()).get();
//        Order orderFind = orderRepository.findById(orderItemExisted.getOrder().getOrderId()).get();
//
//        //Ngay muon
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(orderItemExisted.getBorrowedAt());
//        long borrowTime = cal.getTimeInMillis();
//        //Ngay tra
//        Calendar cal1 = Calendar.getInstance();
//        cal1.setTime(orderItemExisted.getReturnedAt());
//        long returnTime = cal1.getTimeInMillis();
//
//        int day_range = Integer.parseInt(String.valueOf((returnTime - borrowTime)/(1000 * 60 * 60 * 24)));
//
//        if(orderItem.getQuantity() >= 10) {
//            //Trường hợp mượn quá 10 cuốn
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot borrow over 10 book items");
//        }
//        if(orderItem.getQuantity() <= orderItemExisted.getQuantity()){
//            //Trường hợp số sách mượn update <= số sách mượn trước đó
//            bookFind.setAmount(bookFind.getAmount() + (orderItemExisted.getQuantity() - orderItem.getQuantity()) );
//            bookRepository.save(bookFind);
//            //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//            orderFind.setTotalDeposit(orderFind.getTotalDeposit() - (orderItemExisted.getQuantity() - orderItem.getQuantity())*bookFind.getPrice());
//            orderFind.setTotalRent( orderFind.getTotalRent() -
//                    (orderItemExisted.getQuantity() - orderItem.getQuantity())*bookFind.getBorrowPrice()*(day_range));
//            orderRepository.save(orderFind);
//            return ResponseEntity.ok().body(orderItemService.updateOrderItem(order_itemID,orderItem));
//        }else {
//            //Trường hợp số sách mượn update > số sách mượn trước đó
//            if( (orderItem.getQuantity() - orderItemExisted.getQuantity()) >= bookFind.getAmount()){
//                //Số sách mượn thêm vượt quá lượng sách tồn kho
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Store doesn't have enough book! Please decrease your Borrow Book!");
//            }else{
//                //Số sách mượn thêm vừa đủ cho lượng sách tồn kho
//                bookFind.setAmount(bookFind.getAmount() - (orderItem.getQuantity() - orderItemExisted.getQuantity()));
//                bookRepository.save(bookFind);
//                //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//                orderFind.setTotalDeposit(orderFind.getTotalDeposit() + (orderItem.getQuantity() - orderItemExisted.getQuantity())*bookFind.getPrice());
//                orderFind.setTotalRent(orderFind.getTotalRent() +
//                        (orderItem.getQuantity() - orderItemExisted.getQuantity())*bookFind.getBorrowPrice()*(day_range));
//                orderRepository.save(orderFind);
//                return ResponseEntity.ok().body(orderItemService.updateOrderItem(order_itemID,orderItem));
//            }
//        }
//
//       /* orderItem = orderItemService.updateOrderItem(order_itemID, orderItem);
//        return ResponseEntity.ok(orderItem);*/
//    }
//
//    @PutMapping("/order_items/save-buy")
//    public ResponseEntity<?> updateOrderItemWhenBuying(@RequestParam("order_itemID") Long order_itemID ,
//                                                       @RequestBody OrderItem orderItem) {
//        OrderItem orderItemExisted = orderItemRepository.findById(order_itemID).get();
//        Book bookFind = bookRepository.findById(orderItemExisted.getBook().getId()).get();
//        Order orderFind = orderRepository.findById(orderItemExisted.getOrder().getOrderId()).get();
//
//        if(orderItem.getQuantity() <= orderItemExisted.getQuantity()){
//            //Trường hợp số sách mua update <= số sách mua trước đó
//            bookFind.setAmount(bookFind.getAmount() + (orderItemExisted.getQuantity() - orderItem.getQuantity()) );
//            bookRepository.save(bookFind);
//            //Update tổng tiền cọc - depositTotal và tổng tiền thuê - rentTotal trong Order
//            orderFind.setTotalDeposit(orderFind.getTotalDeposit() - (orderItemExisted.getQuantity() - orderItem.getQuantity())*bookFind.getPrice());
//            orderRepository.save(orderFind);
//            return ResponseEntity.ok().body(orderItemService.updateOrderItem(order_itemID,orderItem));
//        }else {
//            //Trường hợp số sách mua update > số sách mua trước đó
//            if( (orderItem.getQuantity() - orderItemExisted.getQuantity()) >= bookFind.getAmount()){
//                //Số sách mua thêm vượt quá lượng sách tồn kho
//                return ResponseEntity.badRequest().body("Store doesn't have enough book! Please decrease your amount of Books!");
//            }else{
//                //Số sách mua thêm vừa đủ cho lượng sách tồn kho
//                bookFind.setAmount(bookFind.getAmount() - (orderItem.getQuantity() - orderItemExisted.getQuantity()));
//                bookRepository.save(bookFind);
//                //Update tổng tiền cọc - depositTotal ( la tong tien thanh toan ) trong Order
//                orderFind.setTotalDeposit(orderFind.getTotalDeposit() + (orderItem.getQuantity() - orderItemExisted.getQuantity())*bookFind.getPrice());
//                orderRepository.save(orderFind);
//                return ResponseEntity.ok().body(orderItemService.updateOrderItem(order_itemID,orderItem));
//            }
//        }
//    }
//
//
//    //Report API
//    @GetMapping("/order_items/date-year")
//    public ResponseEntity<?> getTotalOrderItemInYear(@RequestParam("year") int year){
//        try{
//            return ResponseEntity.ok().body(orderItemService.getListOrderItemInYear(year));
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/date")
//    public List<OrderItem> getList_OrderItem_In_Year_Month(@RequestParam("year") int year,
//                                                           @RequestParam("month") int month){
//        return orderItemService.getListOrderItemInYearAndMonth(year, month);
//    }
//    @GetMapping("/order_items/user-year")
//    public ResponseEntity<?> getOrderItemListByUserID_Per_Year(@RequestParam("userID") Long userID,
//                                                               @RequestParam("year") int year) {
//        try{
//            return ResponseEntity.ok().body(orderItemService.getList_OrderItem_By_UserID_Per_Year(userID, year));
//        } catch (NullPointerException ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/user-date")
//    public ResponseEntity<?> getOrderItemListByUserID_In_Month_Year(@RequestParam("userID") Long userID,
//                                                                    @RequestParam("year") int year,
//                                                                    @RequestParam("month") int month  ) {
//        try{
//            return ResponseEntity.ok().body(orderItemService.getList_OrderItem_By_UserID_In_Month(userID, year, month));
//        } catch (NullPointerException ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/user-date/{userID}")
//    public ResponseEntity<?> getOrderItemListByUserID_In_Month_Year_Path_Variable(@PathVariable Long userID,
//                                                                                  @RequestParam("year") int year,
//                                                                                  @RequestParam("month") int month  ) {
//        try{
//            return ResponseEntity.ok().body(orderItemService.getList_OrderItem_By_UserID_In_Month(userID, year, month));
//        } catch (NullPointerException ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/total-profit")
//    public ResponseEntity<?> getTotalProfitOfStore(){
//        try{
//            return ResponseEntity.ok().body("Total profit of company: "+orderItemService.getTotalProfit()+"$");
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/total-profit-year")
//    public ResponseEntity<?> getTotalProfitOfStoreInYear(@RequestParam("year") int year){
//        try{
//            return ResponseEntity.ok().body("Total profit of company in "+year+": "
//                    +orderItemService.getTotalProfitInYear(year)+"$");
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/total-profit-year-month")
//    public ResponseEntity<?> getTotalProfitOfStoreInYear(@RequestParam("year") int year,
//                                                         @RequestParam("month") int month){
//        try{
//            return ResponseEntity.ok().body("Total profit of company in "+month+"/"+year+": "
//                    +orderItemService.getTotalProfitInMonthOfYear(year,month)+"$");
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/user/total-profit-year")
//    public ResponseEntity<?> getTotalProfitOfUserByYear(@RequestParam("userID") int userID,
//                                                        @RequestParam("year") int year){
//        try{
//            return ResponseEntity.ok().body("Total profit of company in "+year+" from User "+userID+": "
//                    +orderItemService.getTotalProfitOfUserByYear(year,userID)+"$");
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//    @GetMapping("/order_items/user/total-profit-date")
//    public ResponseEntity<?> getTotalProfitOfUserByYear(@RequestParam("userID") int userID,
//                                                        @RequestParam("year") int year,
//                                                        @RequestParam("month") int month){
//        try{
//            return ResponseEntity.ok().body("Total profit of company in "+month +"/"+year+" from User "+userID+": "
//                    +orderItemService.getTotalProfitOfUserByYear_Month(year,month,userID)+"$");
//        }catch (Exception ex){
//            return ResponseEntity.badRequest().body(ex);
//        }
//    }
//
//}
