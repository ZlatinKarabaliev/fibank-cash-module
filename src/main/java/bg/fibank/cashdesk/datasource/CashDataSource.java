package bg.fibank.cashdesk.datasource;

import bg.fibank.cashdesk.dto.CashOperationRequestDTO;

import java.util.List;

/**
 * Interface for loading initial cash operation data from any source (file, DB, API).
 */
public interface CashDataSource {

    /**
     * Loads a list of cash operation requests.
     */
    List<CashOperationRequestDTO> loadOperations();
}
