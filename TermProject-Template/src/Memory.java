import java.util.Arrays;

public class Memory {

	private int memorySize;
	private char[] memory;
	private int[] bitmap;
	private int emptyIndex;

	public Memory(int size) {
		memorySize = size;
		memory = new char[size];

		bitmap= new int[size];
		Arrays.fill(bitmap, 0);
		emptyIndex = 0;
	}

	void addInstructions(char[] buffer, int bufferSize, int BR)
	{
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.memory[i] = buffer[i - BR];
			this.bitmap[i] = 1;
		}

		emptyIndex += bufferSize;
	}

	int addInstructions2(char[] buffer, int bufferSize)
	{

		if (!hasFreeSpace(bufferSize))

			return -1;


		System.out.println("Instructions are adding to memory..");


		for (int i = emptyIndex; i < bufferSize+emptyIndex; i++)
		{
			this.memory[i] = buffer[i - emptyIndex];
			this.bitmap[i]=1;
		}
		//	emptyIndex += bufferSize;

		return emptyIndex;

	}

	boolean hasFreeSpace(int bufferSize)
	{
		int count=0;
		for(int i=0;i<bitmap.length;i++)
		{
			if(this.bitmap[i]==0)
			{
				count++;
			}
			else
				count=0;

			if(count==bufferSize)
			{
				emptyIndex=(i-bufferSize+1);
				System.out.println("There is enough size for process, in memory");
				return true;
			}
		}
		//System.out.println("There is NO enough size for process in memory");
		return false;

	}


	void removeInst(int bufferSize, int BR)
	{
		System.out.println("Instructions are removing from memory..");
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.bitmap[i]=0;
		}


	}


	char[]getInstruction(int PC, int BR)
	{
		char[]instruction = new char[4];
		instruction[0]=memory[PC+BR];
		instruction[1]=memory[PC+BR+1];
		instruction[2]=memory[PC+BR+2];
		instruction[3]=memory[PC+BR+3];

		return instruction;

	}

	int getEmptyIndex()
	{
		return this.emptyIndex;
	}

	public int getMemorySize() {
		return memorySize;
	}

}
