using System;
using System.Collections.Generic;
using System.Text;
using System.ComponentModel;

namespace HebrewEnabledSearcher
{
    public class SearchResult
    {
        string m_Title, m_Path;
        float m_Score;

        public SearchResult(string _title, string _path, float _score)
        {
            this.Title = _title;
            this.Path = _path;
            this.Score = _score;
        }

        [Browsable(false)]
        public string Path
        {
            get { return m_Path; }
            set { m_Path = value; }
        }

        public string Title
        {
            get { return m_Title; }
            set { m_Title = value; }
        }

        public float Score
        {
            get { return m_Score; }
            set { m_Score = value; }
        }
    }
}
