package com.aegisrx.ai;

import com.aegisrx.domain.InteractionReport;
import com.aegisrx.domain.Medication;

import java.util.List;

// ai related stuff
public interface InteractionCheckStrategy {
	List<InteractionReport> checkInteractions(List<Medication> medications);
	String getStrategyName();
}
