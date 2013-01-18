package org.dobots.utility.log;

public class Loggable {
	
	// Should only be true when debugging
	protected boolean m_bDebug;
	
	// receives debug events
	protected ILogListener m_oLogListener = null;
	
	protected static Loggable INSTANCE = null;
	
	protected static Loggable getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Loggable();
		}
		return INSTANCE;
	}

	public void setDebug(boolean i_bDebug) {
		m_bDebug = i_bDebug;
	}

	public void setLogListener(ILogListener listener) {
		this.m_oLogListener = listener;
	}
	
	public void removeLogListener(ILogListener listener) {
		if (this.m_oLogListener == listener) {
			this.m_oLogListener = null;
		}
	}
	
	// wrapper for info logs
	protected void info(String i_strTag, String i_strMessage) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Info, i_strTag, i_strMessage);
		}
	}

	// wrapper for info logs
	protected void info(String i_strTag, String i_strMessage, Throwable i_oObj) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Info, i_strTag, i_strMessage, i_oObj);
		}
	}

	// wrapper for debug logs
	protected void debug(String i_strTag, String i_strMessage) {
		if (m_oLogListener != null) {
			if (m_bDebug) {
				m_oLogListener.onTrace(LogTypes.tt_Debug, i_strTag, i_strMessage);
			}
		}
	}

	// wrapper for debug logs
	protected void debug(String i_strTag, String i_strMessage, Throwable i_oObj) {
		if (m_oLogListener != null) {
			if (m_bDebug) {
				m_oLogListener.onTrace(LogTypes.tt_Debug, i_strTag, i_strMessage, i_oObj);
			}
		}
	}

	// wrapper for warning logs
	protected void warn(String i_strTag, String i_strMessage) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Warning, i_strTag, i_strMessage);
		}
	}

	// wrapper for warning logs
	protected void warn(String i_strTag, String i_strMessage, Throwable i_oObj) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Warning, i_strTag, i_strMessage, i_oObj);
		}
	}

	// wrapper for error logs
	protected void error(String i_strTag, String i_strMessage) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Error, i_strTag, i_strMessage);
		}
	}

	// wrapper for error logs
	protected void error(String i_strTag, String i_strMessage, Throwable i_oObj) {
		if (m_oLogListener != null) {
			m_oLogListener.onTrace(LogTypes.tt_Error, i_strTag, i_strMessage, i_oObj);
		}
	}

}
