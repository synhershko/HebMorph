using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph.LemmaFilters
{
    public class ChainedLemmaFilter : LemmaFilterBase
    {
        public LinkedList<LemmaFilterBase> Filters { get { return _filters; } }
        protected LinkedList<LemmaFilterBase> _filters = new LinkedList<LemmaFilterBase>();

        public override IList<Token> FilterCollection(IList<Token> collection, IList<Token> preallocatedOut)
        {
            if (preallocatedOut == null)
                preallocatedOut = new List<Token>();
            else
                preallocatedOut.Clear();

            bool filteringWasRequired = false;
            LinkedList<LemmaFilterBase>.Enumerator en = _filters.GetEnumerator();
            while (en.MoveNext())
            {
                LemmaFilterBase filter = en.Current;

                if (!filter.NeedsFiltering(collection))
                    continue;

                filteringWasRequired = true;

                foreach (Token t in collection)
                {
                    if (filter.IsValidToken(t))
                        preallocatedOut.Add(t);
                }
            }

            if (filteringWasRequired) return preallocatedOut;

            return null;
        }

        public override bool IsValidToken(Token t)
        {
            throw new NotImplementedException("The method or operation is not implemented.");
        }

        public override bool NeedsFiltering(IList<Token> collection)
        {
            throw new NotImplementedException("The method or operation is not implemented.");
        }
    }
}
