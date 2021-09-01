package test;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	BundleTest.class,
	ConvexHullDistanceTest.class,
	ModelMatrixTest.class,
	OptimumCalculatorTest.class,
	PreferenceTableTest.class,
	TimeSlotTest.class,
	ULListTest.class,
	IRATest.class,
	MAXCUTest.class
})
public class AllTestsSuite {

}
