package com.apposcopy.ella;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.util.MethodUtil;

import com.apposcopy.ella.dexlib2builder.MutableMethodImplementation;
import com.apposcopy.ella.dexlib2builder.BuilderInstruction;
import com.apposcopy.ella.dexlib2builder.MethodLocation;
import com.apposcopy.ella.dexlib2builder.instruction.*;

import java.util.*;
import java.io.*;

/*
 * @author Saswat Anand
 */
public class CallArgInstrumentor extends MethodInstrumentor
{
	protected Map<String,List<Trio>> instrInfo = new HashMap();
	protected Map<Trio,BuilderInstruction> trioToInstruction;

	public CallArgInstrumentor()
	{
		super();
		readInstrInfo();
	}

	private static class Trio
	{
		int offset;
		int argIndex;
		int metadata;
		
		Trio(int offset, int argIndex, int metadata)
		{
			this.offset = offset;
			this.argIndex = argIndex;
			this.metadata = metadata;
		}
	}

	private void readInstrInfo()
	{
		String instrInfoFileName = Config.g().extras.get("ella.iinfo");
		try{
			BufferedReader reader = new BufferedReader(new FileReader(instrInfoFileName));
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(!line.startsWith("METHCALLARG "))
					continue;
				String[] tokens = line.split(" ");
				String methSig = tokens[1];
				int offset = Integer.parseInt(tokens[2]);
				int argIndex = Integer.parseInt(tokens[3]);
				int metadata = Integer.parseInt(tokens[4]);
				Trio trio = new Trio(offset, argIndex, metadata);
				
				List<Trio> trios = instrInfo.get(methSig);
				if(trios == null)
					instrInfo.put(methSig, trios = new ArrayList());
				trios.add(trio);
			}
			reader.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	protected int numRegistersToAdd()
	{
		return 3;
	}

	protected String probeMethName()
	{
		return "v";
	}

	protected String recorderClassName()
	{
		return "com.apposcopy.ella.runtime.ValueRecorder";
	}

	protected void preinstrument(Method method, MutableMethodImplementation code)
	{
		trioToInstruction = new HashMap();

		List<Trio> trios = instrInfo.get(Util.signatureOf(method));
		if(trios == null)
			return;

		List<BuilderInstruction> instructions = code.getInstructions();
		for(BuilderInstruction i : instructions){
			MethodLocation loc = i.getLocation();
			int codeAddress = loc.getCodeAddress();
			for(Trio trio : trios){
				if(codeAddress == trio.offset){
					if(i instanceof Instruction11x){
						Opcode opcode = i.getOpcode();
						assert (opcode == Opcode.MOVE_RESULT ||
								opcode == Opcode.MOVE_RESULT_WIDE ||
								opcode == Opcode.MOVE_RESULT_OBJECT) : opcode.toString();
						i = instructions.get(loc.getIndex()-1);
					}
					if((i instanceof Instruction35c) || (i instanceof Instruction3rc)){
						trioToInstruction.put(trio, i);
						if(trio.argIndex == -1){
							BuilderInstruction nextInstruction = instructions.get(i.getLocation().getIndex()+1);
							Opcode opcode = nextInstruction.getOpcode();
							assert (nextInstruction instanceof Instruction11x) &&
								(opcode == Opcode.MOVE_RESULT ||
								 opcode == Opcode.MOVE_RESULT_WIDE ||
								 opcode == Opcode.MOVE_RESULT_OBJECT) : nextInstruction+ " "+opcode;
						}
					} 	
					else
						assert false : i.toString() + " "+i.getOpcode();
				}
			}
		}
	}

	protected void instrument(Method method, MutableMethodImplementation code, int probeRegister)
	{				
		for(Map.Entry<Trio,BuilderInstruction> entry : trioToInstruction.entrySet()){
			Trio trio = entry.getKey();
			BuilderInstruction instruction = entry.getValue();

			int argRegister = -1;
			boolean refType = false;
			boolean wideType = false;
			int indexToInsertAt = -1;
			if(trio.argIndex >= 0){
				MethodReference callee = null;
				if(instruction instanceof Instruction35c){
					Instruction35c invkInstruction = (Instruction35c) instruction;
					callee = (MethodReference) invkInstruction.getReference();
					CharSequence paramType;
					boolean isStatic = instruction.getOpcode() == Opcode.INVOKE_STATIC;
					if(!isStatic && trio.argIndex == 0)
						refType = true; //this parameter
					else {
						if(isStatic)
							paramType = callee.getParameterTypes().get(trio.argIndex);
						else
							paramType = callee.getParameterTypes().get(trio.argIndex-1);
						char firstChar = paramType.charAt(0);
						if(firstChar == 'J' || firstChar == 'D')
							wideType = true;
						else if(firstChar == '[' || firstChar == 'L')
							refType = true;
					}

					assert trio.argIndex < invkInstruction.getRegisterCount();
					switch(trio.argIndex){
					case 0: 
						argRegister = invkInstruction.getRegisterC();
						break;
					case 1: 
						argRegister = invkInstruction.getRegisterD();
						break;
					case 2:
						argRegister = invkInstruction.getRegisterE();
						break;
					case 3:
						argRegister = invkInstruction.getRegisterF();
						break;
					case 4:
						argRegister = invkInstruction.getRegisterG();
						break;
					default:
						assert false;
					}
				} else if(instruction instanceof Instruction3rc){
					Instruction3rc invkInstruction = (Instruction3rc) instruction;
					assert trio.argIndex < invkInstruction.getRegisterCount();
					callee = (MethodReference) invkInstruction.getReference();
					argRegister = invkInstruction.getStartRegister();
					boolean isStatic = instruction.getOpcode() == Opcode.INVOKE_STATIC_RANGE;
					if(!isStatic && trio.argIndex == 0)
						refType = true;
					else {
						int argCount = isStatic ? 0 : 1;
						for(CharSequence paramType : callee.getParameterTypes()){
							int firstChar = paramType.charAt(0);
							if(argCount == trio.argIndex){
								if(firstChar == 'J' || firstChar == 'D')
									wideType = true;
								else if(firstChar == '[' || firstChar == 'L')
									refType = true;
								break;
							}
							argCount++;
							argRegister += ((firstChar == 'J' || firstChar == 'D') ? 2 : 1);
						}
					}
				} else
					assert false : instruction +" "+ instruction.getOpcode();
				if(callee.getName().equals("<init>") && trio.argIndex == 0)
					//after the invoke instruction, otherwise the var is uninitialized
					indexToInsertAt = instruction.getLocation().getIndex()+1;
				else
					indexToInsertAt = instruction.getLocation().getIndex();  //before the invoke instruction
			} else if(trio.argIndex == -1){
				BuilderInstruction nextInstruction = code.getInstructions().get(instruction.getLocation().getIndex() + 1);
				Opcode opcode = nextInstruction.getOpcode();
				assert (nextInstruction instanceof Instruction11x) &&
					(opcode == Opcode.MOVE_RESULT ||
					 opcode == Opcode.MOVE_RESULT_WIDE ||
					 opcode == Opcode.MOVE_RESULT_OBJECT) : nextInstruction+ " "+opcode;
					
				argRegister = ((Instruction11x) nextInstruction).getRegisterA();
				wideType = opcode ==  Opcode.MOVE_RESULT_WIDE;
				refType = opcode == Opcode.MOVE_RESULT_OBJECT;
				indexToInsertAt = nextInstruction.getLocation().getIndex() + 1;
			}
			else
				assert false : String.valueOf(trio.argIndex);

			assert argRegister >= 0;
			assert !wideType || !refType;
			assert indexToInsertAt >= 0;

			//insert the probe statement at index
			int probeIdNumBits = Instrument.numBits(probeRegister);
			if(probeIdNumBits <= 8)
				code.addInstruction(indexToInsertAt, new BuilderInstruction31i(Opcode.CONST, probeRegister, trio.metadata));
			else
				throw new RuntimeException("TODO: Did not find a register in the range [0..2^8] to store the probe id");

			if(probeIdNumBits == 4 && Instrument.numBits(argRegister) == 4){
				code.addInstruction(indexToInsertAt+1, new BuilderInstruction35c(Opcode.INVOKE_STATIC, 2, probeRegister, argRegister, 0, 0, 0, probeMethRef));
			} else {
				//move the content of argRegister to the register with index probeRegister+1
				Opcode opcode;
				int numArgRegs = 2;
				if(wideType){
					opcode = Opcode.MOVE_WIDE;
					numArgRegs = 3;
				} else if(refType)
					opcode = Opcode.MOVE_OBJECT;
				else
					opcode = Opcode.MOVE;
				code.addInstruction(indexToInsertAt+1, Instrument.moveRegister(probeRegister+1, argRegister, opcode));
				code.addInstruction(indexToInsertAt+2, new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, probeRegister, numArgRegs, probeMethRef));
			}
		}
	}
}
