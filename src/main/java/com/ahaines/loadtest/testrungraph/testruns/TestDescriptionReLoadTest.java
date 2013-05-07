package com.ahaines.loadtest.testrungraph.testruns;

import com.ahaines.loadtest.generators.ProtostuffWrapperUtil;
import com.ahaines.loadtest.testrungraph.PayloadGraphAdapter;
import com.ahaines.loadtest.testrungraph.TestGraphWalker;
import com.ahaines.loadtest.testrungraph.TestGraphWalkerBuilder;
import com.ahaines.loadtest.testrungraph.nodes.AddCoinsAuditChangeNode;
import com.ahaines.loadtest.testrungraph.nodes.BootStrapSessionNode;
import com.ahaines.loadtest.testrungraph.nodes.BuyItemAuditChangeNode;
import com.ahaines.loadtest.testrungraph.nodes.CheckoutUserNode;
import com.ahaines.loadtest.testrungraph.nodes.GraphBuilder;
import com.ahaines.loadtest.testrungraph.nodes.MoveBlockFromInventoryToTowerAuditChangeNode;
import com.ahaines.loadtest.testrungraph.nodes.SellItemAuditChangeNode;

public class TestDescriptionReLoadTest {
	
	public static PayloadGraphAdapter createGraphWalkerTest(ProtostuffWrapperUtil util){
		return new PayloadGraphAdapter(createGraphWalker(util));
	}

	private static TestGraphWalkerBuilder createGraphWalker(final ProtostuffWrapperUtil util) {
		
		return new TestGraphWalkerBuilder(){

			@Override
			public TestGraphWalker buildForUser(long userId) {
				GraphBuilder builder = new GraphBuilder();
				
				builder.addNode(new BootStrapSessionNode(userId))
					   .addNode(new CheckoutUserNode<BootStrapSessionNode>(util))
					   .addNode(new AddCoinsAuditChangeNode<CheckoutUserNode<BootStrapSessionNode>>(util))
					   .addNode(new BuyItemAuditChangeNode<AddCoinsAuditChangeNode<CheckoutUserNode<BootStrapSessionNode>>>(util))
					   .addNode(new MoveBlockFromInventoryToTowerAuditChangeNode<BuyItemAuditChangeNode<AddCoinsAuditChangeNode<CheckoutUserNode<BootStrapSessionNode>>>>(util))
					   .repeat();
				
				return new TestGraphWalker(builder.build());
			}
			
		};
	}

}
