package io.seventytwo.demo.views.order;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.seventytwo.demo.model.order.control.CustomerRepository;
import io.seventytwo.demo.model.order.entity.CustomerInfo;
import io.seventytwo.demo.views.layout.ApplicationLayout;
import org.springframework.data.domain.PageRequest;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringDataSort;

@Route(value = "filtering", layout = ApplicationLayout.class)
@PageTitle("Customers Revenue with FilteringCallback")
public class CustomerRevenueFilteringGridView extends VerticalLayout {

    private Grid<CustomerInfo> grid;
    private CustomerRepository repo;

    public CustomerRevenueFilteringGridView(CustomerRepository customerRepository) {
        repo = customerRepository;

        setHeightFull();

        var filter = new TextField();
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.setPlaceholder("Search");

        filter.addValueChangeListener(e->bindData(e.getValue()));

        add(filter);

        grid = new Grid<>();
        grid.setHeightFull();

        grid.addColumn(CustomerInfo::id).setHeader("ID").setSortable(true).setSortProperty("id");
        grid.addColumn(CustomerInfo::firstname).setHeader("First Name").setSortable(true).setSortProperty("firstname");
        grid.addColumn(CustomerInfo::lastname).setHeader("Last Name").setSortable(true).setSortProperty("lastname");
        grid.addColumn(CustomerInfo::revenue).setHeader("Revenue");

        bindData("");

        add(grid);

    }

    public void bindData(String filter) {
        grid.setItems(
                query -> repo.findAllCustomersWithRevenue(
                PageRequest.of(query.getPage(), query.getPageSize(), toSpringDataSort(query)), filter).stream(),
                query -> repo.countAllByLastnameLikeOrFirstnameLike(filter));
    }

}
