﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Globalization;
using Microsoft.VisualBasic.FileIO;

namespace PreliminaryExperiments
{
    public class Dataset
    {

        public Dataset(List<MachineLearning.Program.Example> examples)
        {
            Samples = examples;
        }

        public Dataset ExtractSample(double rate)
        {
            var newExamples = new List<MachineLearning.Program.Example>();
            var random = new Random((int) DateTime.Now.Ticks);

            foreach (var example in Samples)
            {
                if (random.NextDouble() < rate)
                {
                    newExamples.Add(example);
                }
            }

            foreach (var newExample in newExamples)
            {
                Samples.Remove(newExample);
            }

            return new Dataset(newExamples);
        }

        public List<MachineLearning.Program.Example> Samples { get; private set; }


        public static Dataset FromCsvFile(string path, int labelPos,string labelText, int nrOfFeatures, string delimiter = ",")
        {
            var examples = new List<MachineLearning.Program.Example>();
            var parser = new TextFieldParser(path) {TextFieldType = FieldType.Delimited};

            parser.SetDelimiters(delimiter);
            while (!parser.EndOfData)
            {
                String[] fields = parser.ReadFields();
                if (fields == null) { throw new MissingFieldException("Reading fields from data file did not return any fields."); }

                double label = fields[labelPos] == labelText ? 1 : -1;
                var features = new double[nrOfFeatures];

                int offset = 0;
                for (int index = 1; index < fields.Length-2; index++)
                {
                    if (index == labelPos) { offset = 1; }
                    var field = fields[index + offset];

                    features[index] = Double.Parse(field, CultureInfo.InvariantCulture);
                }

                examples.Add(new MachineLearning.Program.Example(features, label));
            } 

            return new Dataset(examples);
        }


        public List<double> ClearLabels()
        {
            var labels = new List<double>();

            foreach (var example in Samples)
            {
                labels.Add(example.Label);
                example.Label = 0;
            }

            return labels;
        }

        public void Shuffle()
        {
            var rnd = new Random();
            for (var i = 0; i < Samples.Count; i++)
                Swap(Samples, i, rnd.Next(i, Samples.Count));
        }

        public void Swap(IList list, int i, int j)
        {
            var temp = list[i];
            list[i] = list[j];
            list[j] = temp;
        }
    }
}
