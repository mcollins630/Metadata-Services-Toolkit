package xc.mst.services.normalization;

import java.util.ArrayList;
import java.util.List;

import xc.mst.bo.provider.Format;
import xc.mst.bo.provider.Set;
import xc.mst.bo.record.Record;
import xc.mst.dao.DataException;
import xc.mst.dao.service.DefaultOaiIdentiferForServiceDAO;
import xc.mst.manager.IndexException;
import xc.mst.manager.record.DBRecordService;
import xc.mst.utils.TimingLogger;

public class DBNormalizationService extends NormalizationService {

	public void init() {
		setOaiIdentifierForServiceDAO(new DBOaiIdentiferForServiceDAO());
	}
	
	@Override
	public boolean processRecords() {
		
		try {
			List<Record> inputRecords  = ((DBRecordService)getRecordService()).getInputForServiceToProcess(service.getId(), true);
			Record r2 = null;
			Format f = getFormatService().getFormatByName("marcxml");
			Set s = getSetService().getSetBySetSpec("norm_output_set_spec");
			List<Set> sets = new ArrayList<Set>();
			sets.add(s);
			while (inputRecords != null && inputRecords.size() > 0) {
				for (Record r : inputRecords) {
					r.setService(service);
					r.setFormat(f);
					r.setSets(sets);
					try {
						processRecord(r);
					} catch (Throwable t) {
						TimingLogger.log("ouch - problems");
						log.error("r.getId(): "+r.getId());
						log.error("t", t);
					}
					getRecordService().update(r);
					r2 = r;
				}
				((DBRecordService)getRecordService()).commit(service.getId(), true);
				inputRecords  = getRecordService().getInputForServiceToProcess(service.getId());
			}
			if (r2 != null) {
				checkProcessingDirectives(r2);
			}
			((DBRecordService)getRecordService()).commit(service.getId(), true);
			((DBRecordService)getRecordService()).reset();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}
	
	@Override
	protected void insertNewRecord(Record record) throws DataException, IndexException {
		getRecordService().insert(record);
	}
	
	public class DBOaiIdentiferForServiceDAO extends DefaultOaiIdentiferForServiceDAO {
		protected int id=0;
		
		@Override
		public long getNextOaiIdForService(int serviceId) {
			return id++;
		}
		
	}
	
}