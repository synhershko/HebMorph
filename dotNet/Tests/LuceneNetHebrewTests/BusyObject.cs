using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;

namespace LuceneNetHebrewTests
{
    /// <summary>
    /// This class allows a consumer Control to easily become busy for long operations, without worrying
    /// about returning to available state in case of errors, exceptions or when completed the task.
    /// 
    /// Usage:
    ///     using (new BusyObject(this)) { /* lengthy operations */ }
    /// 
    /// Where "this" is a Control or a Form to take into a Busy state.
    /// </summary>
    public class BusyObject : IDisposable
    {
        Control m_ctrl;

        public BusyObject(Control ctrl)
        {
            m_ctrl = ctrl;
            m_ctrl.UseWaitCursor = true;
            m_ctrl.Enabled = false;
        }

        #region IDisposable Members

        public void Dispose()
        {
            m_ctrl.Enabled = true;
            m_ctrl.UseWaitCursor = false;
        }

        #endregion
    }
}
