#include <fftw3.h>
#include "addiLib.h"

fftw_complex *in, *out;

JNIEXPORT jobjectArray JNICALL Java_com_addi_toolbox_general_fft_fftNative (JNIEnv * env, jobject thiz, jdoubleArray inReal, jdoubleArray inImag, jint size) {

	fftw_plan p;
	int i;
	int j;
	jboolean isCopyReal;
	jdouble* srcArrayRealElems;
	jboolean isCopyImag;
	jdouble* srcArrayImagElems;

	srcArrayRealElems = (*env)->GetDoubleArrayElements(env, inReal, &isCopyReal);
	srcArrayImagElems = (*env)->GetDoubleArrayElements(env, inImag, &isCopyImag);

	in = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * size);
	out = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * size);
	for (i=0; i<size; i++) {
		in[i][0] = srcArrayRealElems[i];
		in[i][1] = srcArrayImagElems[i];
	}
	p = fftw_plan_dft_1d(size, in, out, FFTW_FORWARD, FFTW_ESTIMATE);
	fftw_execute(p); /* repeat as needed */
	fftw_destroy_plan(p);

	// Create the returnable 2D array

	// Get the float array class
	jclass doubleArrayClass = (*env)->FindClass(env, "[D");

	jobjectArray myReturnable2DArray = (*env)->NewObjectArray(env, (jsize) size, doubleArrayClass, NULL);

	// Go through the first dimension and add the second dimension arrays
	for (j = 0; j < size; j++)
	{
		jdoubleArray doubleArray = (*env)->NewDoubleArray(env, 2);
		(*env)->SetDoubleArrayRegion(env, doubleArray, (jsize) 0, (jsize) 2, out[j]);
	    (*env)->SetObjectArrayElement(env, myReturnable2DArray, (jsize) j, doubleArray);
	    (*env)->DeleteLocalRef(env, doubleArray);
	}

	// Return a Java consumable 2D double array
	return myReturnable2DArray;

}

JNIEXPORT jint JNICALL Java_com_addi_toolbox_general_fft_fftCleanup (JNIEnv * env, jobject thiz) {
	fftw_free(in);
	fftw_free(out);
	return 0;
}
