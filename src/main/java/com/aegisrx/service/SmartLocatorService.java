package com.aegisrx.service;

import com.aegisrx.dao.PharmacyDAO;
import com.aegisrx.dao.DrugProfileDAO;
import com.aegisrx.domain.Pharmacy;
import com.aegisrx.domain.DrugProfile;

import java.sql.SQLException;
import java.util.List;

// processing logic
public class SmartLocatorService {

	private final PharmacyDAO pharmacyDAO;
	private final DrugProfileDAO drugProfileDAO;

	public SmartLocatorService() {
		this.pharmacyDAO = new PharmacyDAO();
		this.drugProfileDAO = new DrugProfileDAO();
	}

public List<Pharmacy> locateMedicine(int drugId, String region) throws SQLException {
		List<Pharmacy> results = pharmacyDAO.findWithDrugInStock(drugId, region);

		if (results.isEmpty()) {
			System.out.println("[SmartLocator] No pharmacies in " + region + " have verified stock.");
		} else {
			System.out.println("[SmartLocator] Found " + results.size() + " pharmacies with verified stock.");
		}

		return results;
	}

	public List<DrugProfile> searchDrugs(String name) throws SQLException {
		return drugProfileDAO.searchByName(name);
	}

	public List<DrugProfile> getAllDrugs() throws SQLException {
		return drugProfileDAO.findAll();
	}
}
