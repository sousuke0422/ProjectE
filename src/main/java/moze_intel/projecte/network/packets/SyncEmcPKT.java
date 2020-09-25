package moze_intel.projecte.network.packets;

import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.SimpleStack;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.PELogger;

import java.util.List;

public class SyncEmcPKT implements IMessage
{
	private int packetNum;
	private Object[] data;

	public SyncEmcPKT() {}

	public SyncEmcPKT(int packetNum, List<Long[]> arrayList)
	{
		this.packetNum = packetNum;
		data = arrayList.toArray();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		packetNum = buf.readInt();
		int size = buf.readInt();
		data = new Object[size];

		for (int i = 0; i < size; i++)
		{
			Long[] array = new Long[4];

			for (int j = 0; j < 4; j++)
			{
				array[j] = buf.readLong();
			}

			data[i] = array;
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(packetNum);
		buf.writeInt(data.length);

		for (Object obj : data)
		{
			Long[] array = (Long[]) obj;

			for (int i = 0; i < 4; i++)
			{
				buf.writeLong(array[i]);
			}
		}
	}

	public static class Handler implements IMessageHandler<SyncEmcPKT, IMessage>
	{
		@Override
		public IMessage onMessage(final SyncEmcPKT pkt, MessageContext ctx)
		{
			if (pkt.packetNum == 0)
			{
				PELogger.logInfo("Receiving EMC data from server.");

				EMCMapper.emc.clear();
				EMCMapper.emc = Maps.newLinkedHashMap();
			}

			for (Object obj : pkt.data)
			{
				Long[] array = (Long[]) obj;
				int id = Math.toIntExact(array[0]);
				int size = Math.toIntExact(array[1]);
				int damage = Math.toIntExact(array[2]);
				long emc;

				SimpleStack stack = new SimpleStack(id, size, damage);

				if (stack.isValid())
				{
					EMCMapper.emc.put(stack, Long.valueOf(array[3]));
				}
			}

			if (pkt.packetNum == -1)
			{
				PELogger.logInfo("Received all packets!");

				Transmutation.cacheFullKnowledge();
				FuelMapper.loadMap();
			}
			return null;
		}
	}

	public static class EmcPKTInfo {
		private int id, damage;
		private long emc;

		@Deprecated
		public EmcPKTInfo(int id, int damage, long emc) {
			this.id = id;
			this.damage = damage;
			this.emc = emc;
		}

		@Deprecated
		public int getDamage() {
			return damage;
		}

		@Deprecated
		public int getId() {
			return id;
		}

		@Deprecated
		public long getEmc() {
			return emc;
		}
	}
}
