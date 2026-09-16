import importlib.util
import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch


MODULE_PATH = Path(__file__).with_name("ai_test_planner.py")
SPEC = importlib.util.spec_from_file_location("ai_test_planner", MODULE_PATH)
planner = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules[SPEC.name] = planner
SPEC.loader.exec_module(planner)


class PlannerTest(unittest.TestCase):

    @patch.dict(os.environ, {}, clear=True)
    def test_checkout_query_retrieves_documented_cases(self):
        plan = planner.build_plan("guest checkout login regression")

        self.assertEqual(plan["mode"], "offline_bm25")
        self.assertTrue(plan["review_required"])
        self.assertTrue(
            any(
                citation["source"] == "docs/qa-test-plan.md"
                for citation in plan["citations"]
            )
        )
        self.assertTrue(
            {"TC-01", "TC-02"}.issubset(
                {case["case_id"] for case in plan["documented_test_cases"]}
            )
        )
        self.assertIn("selenium-checkout-smoke", plan["allowlisted_profiles"])

    @patch.dict(os.environ, {}, clear=True)
    def test_payment_query_maps_only_known_runner(self):
        plan = planner.build_plan("Stripe webhook replay duplicate payment")

        self.assertTrue(set(plan["allowlisted_profiles"]).issubset(planner.RUNNERS))
        self.assertIn("stripe-webhook-unit", plan["allowlisted_profiles"])


if __name__ == "__main__":
    unittest.main()
