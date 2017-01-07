using System.Collections.Generic;

namespace HebMorph.DataStructures
{
    public class DictHebMorph
    {
        private Dictionary<string, int> pref = new Dictionary<string, int>();
        private readonly DictRadix<MorphData> dict = new DictRadix<MorphData>();
        private readonly Dictionary<string, MorphData> mds = new Dictionary<string, MorphData>();

        public void AddNode(string s, MorphData md)
        {
            this.mds.Add(s, md);
            this.dict.AddNode(s, md);
        }

        public DictRadix<MorphData> GetRadix()
        {
            return dict;
        }

        public Dictionary<string, int> GetPref()
        {
            return pref;
        }

        public void SetPref(Dictionary<string, int> prefs)
        {
            this.pref = prefs;
        }

        public MorphData Lookup(string key)
        {
            MorphData ret;
            if (mds.TryGetValue(key, out ret))
                return ret;
            return null;
        }

        public void Clear()
        {
            dict.Clear();
            pref.Clear();
            mds.Clear();
        }
    }
}
