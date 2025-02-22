package io.seventytwo.demo.views.order;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.seventytwo.demo.model.order.entity.CustomerInfo;
import io.seventytwo.demo.views.layout.ApplicationLayout;
import io.seventytwo.vaadinjooq.util.VaadinJooqUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import static io.seventytwo.demo.database.tables.Customer.CUSTOMER;
import static io.seventytwo.demo.database.tables.OrderItem.ORDER_ITEM;
import static io.seventytwo.demo.database.tables.Product.PRODUCT;
import static io.seventytwo.demo.database.tables.PurchaseOrder.PURCHASE_ORDER;
import static org.jooq.impl.DSL.lower;

@Route(value = "jooq", layout = ApplicationLayout.class)
@PageTitle("Customers Revenue with jOOQ")
public class CustomerRevenueJooqGridView extends VerticalLayout {

    private final DSLContext dsl;
    private final Grid<CustomerInfo> grid;

    public CustomerRevenueJooqGridView(DSLContext dsl) {
        this.dsl = dsl;

        setHeightFull();

        TextField filter = new TextField();
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.addValueChangeListener(event -> loadData(event.getValue()));
        filter.setPlaceholder("Search");

        add(filter);

        grid = new Grid<>();
        grid.addColumn(CustomerInfo::id).setHeader("ID").setSortable(true).setSortProperty(CUSTOMER.ID.getName());
        grid.addColumn(CustomerInfo::firstname).setHeader("First Name").setSortable(true).setSortProperty(CUSTOMER.FIRSTNAME.getName());
        grid.addColumn(CustomerInfo::lastname).setHeader("Last Name").setSortable(true).setSortProperty(CUSTOMER.LASTNAME.getName());
        grid.addColumn(CustomerInfo::revenue).setHeader("Revenue");

        grid.setHeightFull();

        loadData("");

        add(grid);
    }

    private void loadData(String name) {
        grid.setItems(query -> dsl
                .select(CUSTOMER.ID, CUSTOMER.LASTNAME, CUSTOMER.FIRSTNAME, DSL.sum(PRODUCT.PRICE))
                .from(CUSTOMER)
                .join(PURCHASE_ORDER).on(PURCHASE_ORDER.CUSTOMER_ID.eq(CUSTOMER.ID))
                .join(ORDER_ITEM).on((ORDER_ITEM.ORDER_ID.eq(PURCHASE_ORDER.ID)))
                .join(PRODUCT).on((PRODUCT.ID.eq(ORDER_ITEM.PRODUCT_ID)))
                .where(createCondition(name))
                .groupBy(CUSTOMER.ID)
                .orderBy(VaadinJooqUtil.orderFields(CUSTOMER, query))
                .offset(query.getOffset())
                .limit(query.getLimit())
                .fetchStreamInto(CustomerInfo.class)
        );
    }

    private Condition createCondition(String name) {
        Condition condition = DSL.noCondition();
        if (!name.isBlank()) {
            condition = lower(CUSTOMER.LASTNAME).like(name.toLowerCase() + "%")
                    .or(lower(CUSTOMER.FIRSTNAME).like(name.toLowerCase() + "%"));
        }
        return condition;
    }
}
