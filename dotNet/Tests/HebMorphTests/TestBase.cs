using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace HebMorph.Tests
{
	[TestClass]
	public abstract class TestBase
	{
		protected static string hspellPath;

		[TestInitialize]
		public virtual void SetUp()
		{
			string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
			int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
			if (loc > -1)
			{
				path = path.Remove(loc + 1);
				hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
			}
		}
	}
}
