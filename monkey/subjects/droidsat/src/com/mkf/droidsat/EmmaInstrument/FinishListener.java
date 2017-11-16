package arity.calculator.EmmaInstrument;


public interface FinishListener {
	void onActivityFinished();
	void dumpIntermediateCoverage(String filePath);
}
